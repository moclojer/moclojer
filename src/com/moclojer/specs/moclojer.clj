(ns com.moclojer.specs.moclojer
  (:require
   [clojure.data.json :as json]
   [clojure.set :refer [rename-keys]]
   [clojure.string :as string]
   [com.moclojer.external-body.core :as ext-body]
   [com.moclojer.log :as log]
   [com.moclojer.webhook :as webhook]
   [malli.provider :as mp]
   [reitit.swagger :as swagger]
   [selmer.parser :as selmer]))

(defn render-template
  "Renders given `template`, using `request`'s data."
  [template request]
  (try
    {:content (selmer/render (ext-body/->str template) request)}
    (catch Exception e
      (log/log :error :bad-body
               :body template
               :message (.getMessage e))
      {:error? true
       :content (json/write-str {:message (.getMessage e)})})))

(defn enrich-external-body
  "Enriches the external body with a resolved path."
  [external-body request]
  (let [{:keys [content]} (render-template (:path external-body) request)]
    (assoc external-body :path content)))

(defn build-body
  "Builds the body from the response."
  [response request]
  (let [{:keys [content error?]} (render-template
                                  (if-let [?external-body (:external-body response)]
                                    (-> ?external-body
                                        (enrich-external-body request)
                                        ext-body/type-identification)
                                    (:body response))
                                  request)]
    (try
      (cond
        error? (let [parsed (json/read-str content :key-fn keyword)]
                 {:error parsed
                  :message (get parsed :message)})
        (string? content) (try
                            (json/read-str content)
                            (catch Exception e
                              (log/log :warn :json-parse-error
                                       :body content
                                       :message (.getMessage e))
                              content))
        :else content)
      (catch Exception e
        (log/log :error :bad-body
                 :body content
                 :message (.getMessage e))
        {:error (.getMessage e)}))))

(defn assoc-if [m k v]
  (if v
    (assoc m k v)
    m))

(defn webhook-condition
  "Checks `if` condition and return boolean value, default is true"
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

(defn generic-reitit-handler
  "Builds a reitit handler that responds with pre-defined `response`.

  Since we also support `webhooks`, given `webhook-config` is used
  when necessary in together with the `response`."
  [response webhook-config]
  (fn [request]
    (when webhook-config
      (webhook/request-after-delay
       {:url (:url webhook-config)
        :condition (webhook-condition (:if webhook-config) request)
        :method (:method webhook-config)
        :body (:content (render-template (:body webhook-config) request))
        :headers (or (json/read-str
                      (render-template
                       (reduce-kv
                        (fn [acc k v]
                          (assoc acc k (string/lower-case v)))
                        {}
                        (:headers webhook-config))
                       request))
                     (:headers request))
        :sleep-time (:sleep-time webhook-config)}))
    (let [parameters (build-parameters (:parameters request))
          body-result (build-body response parameters)]
      (log/log :info :body body-result)
      (if (and (map? body-result) (:error body-result))
        {:body (json/write-str body-result)
         :status 500
         :headers {"Content-Type" "application/json"}}
        {:body (json/write-str body-result)
         :status (:status response 200)
         :headers (into
                   {"Content-Type" "application/json"}
                   (map (fn [[k v]]
                          [(name k) (str v)]))
                   (:headers response))}))))

(defn generate-route-name
  [host path method]
  (str method "-" (or host "localhost") "-" (string/replace (string/replace path "/" "") ":" "--")))

(defn generate-method
  "Adapts given `method` to a normalized string version of itself."
  [method]
  (string/lower-case (name (or method "get"))))

(defn provide [val & [cond forced-type]]
  (if cond
    (or forced-type (mp/provide [val]))
    val))

(defn make-path-parameters
  "Based on `path`'s declared type, provides a placeholder that can be
  used later on by reitit to understand the param's data type."
  [path & [gen?]]
  (-> (fn [query-types s]
        (if (string/starts-with? s ":")
          (let [[param-name
                 param-type] (-> (string/replace s #":" "") (string/split  #"\|"))
                fun (condp = param-type
                      "int" (provide (int 0) gen?)
                      "string" (provide "example" gen?)
                      "bool" (provide true gen?)
                      "float" (provide (float 0.0) gen? :float)
                      "double" (provide (double 0.0) gen? :double)
                      nil (provide "example" gen?))]
            (assoc query-types (keyword param-name) fun))
          query-types))
      (reduce {} (string/split path #"/"))))

(defn mock-response-body-request
  "Given a `body`, generates a request based on the used variables."
  [?body parameters]
  (when ?body
    (select-keys
     (rename-keys parameters {:path :path-params
                              :body :json-params})
     (try
       (seq (selmer/known-variables (ext-body/->str ?body)))
       (catch Exception e
         (log/log :error :bad-body
                  :body ?body
                  :message (.getMessage e))
         [])))))

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

(defn make-query-parameters [query & [gen?]]
  (reduce-kv
   (fn [acc k v]
     (assoc acc (keyword k)
            (condp = v
              "int" (provide (int 0) gen?)
              "string" (provide "example" gen?)
              "bool" (provide gen? true)
              "float" (provide (float 0.0) gen? :float)
              "double" (provide (double 0.0) gen? :double)
              nil (provide "example"))))
   {}
   query))

(defn make-body
  [?body type]
  (when ?body
    (try
      (mp/provide
       [(json/read-str ?body :key-fn keyword)])
      (catch Exception e
        (log/log :error :bad-body
                 :type type
                 :body ?body
                 :message (.getMessage e))))))

(defn ->reitit
  "Adapts given moclojer endpoints to reitit's data based routes, while
  parsing request data types throughout the way."
  [spec]
  (->> (for [[[host path method tag] endpoints]
             (group-by (juxt :host :path #(generate-method (:method %)) :tag)
                       (remove nil? (map :endpoint spec)))]
         (let [method (generate-method method)
               route-name (generate-route-name host path method)
               endpoint (first endpoints)
               response (:response endpoint)
               real-path (create-url path)
               rate-limit (:rate-limit endpoint)
               create-params-fn #(create-swagger-parameters
                                  (make-path-parameters path %)
                                  (make-query-parameters (:query endpoint) %)
                                  (make-body (:body endpoint) :request))]

           [real-path
            {:data {:host (or host "localhost")
                    :rate-limit rate-limit}
             (keyword method)
             {:summary (if-not (string/blank? real-path)
                         (str "Generated from " real-path)
                         "Auto-generated")
              :swagger {:tags [(or tag route-name)]}
              :parameters (create-params-fn true)
              :responses {(or (:status response) 200)
                          {:body any?}}
              :handler (generic-reitit-handler response nil)}}]))
       (remove nil?)
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
