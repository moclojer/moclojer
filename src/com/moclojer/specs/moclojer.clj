(ns com.moclojer.specs.moclojer
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]
   [com.moclojer.external-body.core :as ext-body]
   [com.moclojer.log :as log]
   [com.moclojer.webhook :as webhook]
   [reitit.swagger :as swagger]
   [selmer.parser :as selmer]))

(def primitives
  [int? string? boolean? float? double?])

(def wrapped-primitive-fns
  "Wrappes primitive functions so they return themselves incase of truthness.

  Useful with `some`."
  (map (fn [primitive-fn]
         #(when (primitive-fn %)
            primitive-fn))
       primitives))

(def malli-primitives
  [:int :string :boolean :float :double])

(defn ->primitive-malli
  [primitive]
  (or (get (zipmap primitives malli-primitives) primitive)
      (throw (ex-info "primitive not supported"
                      {:primitive primitive}))))

(defn render-template
  [template request]
  (selmer/render (ext-body/->str template) request))

(defn enrich-external-body
  "Enriches the external body with a resolved path."
  [external-body request]
  (let [path (render-template (:path external-body) request)]
    (assoc external-body :path path)))

(defn build-body
  "Builds the body from the response."
  [response request]
  (let [external-body (:external-body response)]
    (cond
      external-body
      (-> external-body
          (enrich-external-body request)
          ext-body/type-identification
          (render-template request))
      :else (-> (:body response)
                (render-template request)))))

(defn assoc-if [m k v]
  (if (seq v)
    (assoc m k v)
    m))

(defn webhook-condition
  "check `if` condition and return boolean value, default is true"
  [condition request]
  (let [template (str "{% if " condition " %}true{% else %}false{% endif %}")]
    (if (empty? condition)
      true
      (boolean (Boolean/valueOf (selmer/render (ext-body/->str template) request))))))

(defn build-parameters [request-values]
  (let [query (:query request-values)
        path (:path request-values)
        body (:body request-values)]
    (-> (assoc-if {} :query-params query)
        (assoc-if :path-params path)
        (assoc-if :json-params body))))

(defn generic-reitit-handler [response
                              webhook-config]
  (fn [request]
    (prn request)
    (when webhook-config
      (webhook/request-after-delay
       {:url (:url webhook-config)
        :condition (webhook-condition (:if webhook-config) request)
        :method (:method webhook-config)
        :body (render-template (:body webhook-config) request)
        :headers (:headers webhook-config)
        :sleep-time (:sleep-time webhook-config)}))
    (let [parameters (build-parameters (:parameters request))
          body (build-body response parameters)]
      (log/log :info :body (json/read-str body :key-fn keyword))
      {:body  body
       :status 200
       :headers (into
                 {}
                 (map (fn [[k v]]
                        [(name k) (str v)]))
                 (:headers response))})))

(defn generate-route-name
  [host path method]
  (str method "-" (or host "localhost") "-" (string/replace (string/replace path "/" "") ":" "--")))

(defn generate-method [method]
  (-> (or method "get")
      name
      string/lower-case))

(defn make-parameters [path]
  (reduce
   (fn [query-types s]
     (if (string/starts-with? s ":")
       (let [[param-name param-type] (-> (string/replace s #":" "") (string/split  #"\|"))
             fun (condp = param-type
                   "int" int?
                   "string" string?
                   "bool" boolean?
                   "float" float?
                   "double" double?
                   nil string?)]
         (assoc query-types (keyword param-name) fun))
       query-types))
   {} (string/split path #"/")))

(defn create-url [pattern]
  (let [segments (string/split pattern #"/")
        processed-segments (map (fn [segment]
                                  (if (re-find #":" segment)
                                    (first (string/split segment #"\|"))
                                    segment))
                                segments)]
    (string/join "/" processed-segments)))

(defn create-swagger-parameters [path query body]
  (-> (assoc-if {} :path path)
      (assoc-if :query query)
      (assoc-if :body body)))

(defn make-query-parameters [query]
  (reduce-kv (fn [acc k v]
               (assoc acc (keyword k) (condp = v
                                        "int" int?
                                        "string" string?
                                        "bool" boolean?
                                        "float" float?
                                        "double" double?
                                        nil string?))) {} query))

(defn make-body-parameters
  "Given a `body`, maps each value to a malli primitive.

  {:hello \"123\"
   :bye {:bye2 123
         :bye3 true}} => {:hello :string, :bye {:bye2 :int :bye3 :boolean}}"
  [body]
  (reduce-kv
   (fn [acc k v]
     (assoc acc
            (keyword k)
            (if (map? v)
              (make-body-parameters v)
              (-> (some #(% v) wrapped-primitive-fns)
                  (->primitive-malli)))))
   {}
   body))

(defn ->reitit
  [spec]
  (->> (for [[[host path method tag] endpoints]
             (group-by (juxt :host :path :method)
                       (remove nil? (map :endpoint spec)))]
         (let [method (generate-method method)
               route-name (generate-route-name host path method)
               response (:response (first endpoints))
               real-path (create-url path)]

           [real-path
            {:host (or host "localhost")
             :swagger {:tags [(or tag route-name)]}
             :parameters (create-swagger-parameters
                          (make-parameters path)
                          (make-query-parameters (:query (first endpoints)))
                          (make-body-parameters (:body (first endpoints))))
             :responses {(or (:status response) 200)
                         (try
                           {:body (-> (json/read-str response :key-fn keyword)
                                      (:body)
                                      (make-body-parameters))}
                           (catch Exception e
                             (log/log :error ::bad-response-body (.getMessage e))
                             {:body :string}))}
             (keyword method) {:summary (if real-path
                                          (str "Generated from " real-path)
                                          "Auto-generated")
                               :handler (generic-reitit-handler response nil)}}]))
       (concat [["/swagger.json"
                 {:get {:no-doc true
                        :swagger {:info {:title "moclojer-mock"
                                         :description "my mock"}}
                        :handler (swagger/create-swagger-handler)}}]])
       (reduce
        (fn [routes [route-name route-definition]]
          (let [?existing-definitions (get routes route-name)]
            (assoc routes route-name
                   (merge ?existing-definitions route-definition))))
        {})
       (map identity)
       (vec)))
