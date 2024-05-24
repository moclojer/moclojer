(ns com.moclojer.specs.moclojer
  (:require
   [clojure.string :as string]
   [com.moclojer.external-body.core :as ext-body]
   [com.moclojer.webhook :as webhook]
   [io.pedestal.http.route :as route]
   [reitit.swagger :as swagger]
   [selmer.parser :as selmer]
   [com.moclojer.log :as log]
   [clojure.data.json :as json]
   [clojure.edn :as edn]))

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

(defn webhook-condition
  "check `if` condition and return boolean value, default is true"
  [condition request]
  (let [template (str "{% if " condition " %}true{% else %}false{% endif %}")]
    (if (empty? condition)
      true
      (boolean (Boolean/valueOf (selmer/render (ext-body/->str template) request))))))

(defn generic-handler
  [response webhook-config]
  (fn [request]
    (when webhook-config
      (webhook/request-after-delay
       {:url (:url webhook-config)
        :condition (webhook-condition (:if webhook-config) request)
        :method (:method webhook-config)
        :body (render-template (:body webhook-config) request)
        :headers (:headers webhook-config)
        :sleep-time (:sleep-time webhook-config)}))
    {:body    (build-body response request)
     :status  (:status response)
     :headers (into
               {}
               (map (fn [[k v]]
                      [(name k) (str v)]))
               (:headers response))}))

(defn generic-reitit-handler [response
                              webhook-config]
  (fn [request]
    (when webhook-config
      (webhook/request-after-delay
       {:url (:url webhook-config)
        :condition (webhook-condition (:if webhook-config) request)
        :method (:method webhook-config)
        :body (render-template (:body webhook-config) request)
        :headers (:headers webhook-config)
        :sleep-time (:sleep-time webhook-config)}))
    (let [body (build-body response (:parameters request))]
      (log/log :info :body (json/read-str body :key-fn keyword))
      {:body  body
       :status (:status response)
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

(defn ->pedestal
  "generate routes from moclojer spec to pedestal"
  [spec]
  (->>
   (for [[[host path method] endpoints] (group-by (juxt :host :path :method)
                                                  (remove nil? (map :endpoint spec)))]
     (let [method (generate-method method)
           route-name (generate-route-name host path method)
           response (:response (first endpoints))
           webhook-config (:webhook (first endpoints))]
       (route/expand-routes
        #{{:host host}
          [path
           (keyword method)
           (generic-handler response webhook-config)
           :route-name (keyword route-name)]})))
   (mapcat identity)))

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

(defn assoc-if [m k v]
  (if (seq v)
    (assoc m k v)
    m))

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

(defn make-body-parameters [body]
  (reduce-kv (fn [acc k v]
               (assoc acc (keyword k) (condp = v
                                        "int" int?
                                        "string" string?
                                        "bool" boolean?
                                        "float" float?
                                        "double" double?
                                        nil string?))) {} body))

(defn ->reitit
  [spec]
  (concat
   [["/swagger.json"
     {:get {:no-doc true
            :swagger {:info {:title "moclojer-mock"
                             :description "my mock"}}
            :handler (swagger/create-swagger-handler)}}]]
   (for [[[host path method tag] endpoints]
         (group-by (juxt :host :path :method)
                   (remove nil? (map :endpoint spec)))]
     (let [method (generate-method method)
           route-name (generate-route-name host path method)
           response (:response (first endpoints))
           real-path (create-url path)]

       [real-path
        {:host (or host "localhost")
         :swagger {:tags [(or tag route-name)]}
         :parameters (create-swagger-parameters (make-parameters path)
                                                (make-query-parameters (:query (first endpoints)))
                                                (make-body-parameters (:body (first endpoints))))
         :responses {(or (:status response) 200) {:body string?}}
         (keyword method) {:summary (str "Generated from " real-path)
                           :handler (generic-reitit-handler response nil)}}]))))

