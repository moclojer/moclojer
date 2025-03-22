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
   [selmer.parser :as selmer]
   [org.httpkit.server :as http-kit]))

(defn ws-send!
  "Sends a message to a WebSocket connection"
  [channel message]
  (try
    (http-kit/send! channel message)
    (catch Exception e
      (log/log :error :websocket-send-error
               :message (.getMessage e)))))

(defn render-template
  "Renders given `template`, using `request`'s data."
  [template request]
  (try
    {:content (-> template
                  ext-body/->str
                  (selmer/render request))}
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

(defn parse-json-safely
  "Tries to parse a JSON, returning the original content in case of error"
  [content]
  (try
    (json/read-str content)
    (catch Exception e
      (log/log :warn :json-parse-error
               :body content
               :message (.getMessage e))
      content)))

(defn get-response-body
  "Gets the correct response body (processing external-body if necessary)"
  [response request]
  (if-let [?external-body (:external-body response)]
    (-> ?external-body
        (enrich-external-body request)
        ext-body/type-identification)
    (:body response)))

(defn build-body
  "Builds the body from the response."
  [response request]
  (try
    (let [{:keys [content error?]} (-> response
                                       (get-response-body request)
                                       (render-template request))]
      (cond
        error?
        (-> content
            (json/read-str :key-fn keyword)
            (as-> parsed {:error parsed
                          :message (:message parsed)}))

        (string? content)
        (parse-json-safely content)

        :else content))
    (catch Exception e
      (log/log :error :bad-body
               :body (:body response)
               :message (.getMessage e))
      {:error (.getMessage e)})))

(defn assoc-if [m k v]
  (if v
    (assoc m k v)
    m))

(defn webhook-condition
  "Checks `if` condition and return boolean value, default is true when condition is empty"
  [condition request]
  (if (empty? condition)
    true
    (let [template (str "{% if " condition " %}true{% else %}false{% endif %}")]
      (-> template
          ext-body/->str
          (selmer/render request)
          Boolean/valueOf
          boolean))))

(defn build-parameters [request-values]
  (let [query (:query request-values)
        path (:path request-values)
        body (:body request-values)]
    (-> {}
        (assoc-if :query-params query)
        (assoc-if :path-params path)
        (assoc-if :json-params body))))

(defn process-ws-message
  "Process WebSocket message based on pattern matching"
  [channel message pattern-configs request-params]
  (let [params (assoc request-params :message message)]
    (doseq [{:keys [pattern response]} pattern-configs]
      (let [matches (boolean (re-matches (re-pattern pattern) message))]
        (when matches
          (when-let [content (:content (render-template response params))]
            (ws-send! channel content)))))))

(defn handle-ws-connect
  "Handle WebSocket connection"
  [channel connect-config request-params]
  (when-let [response (:response connect-config)]
    (when-let [content (:content (render-template response request-params))]
      (ws-send! channel content))))

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

(defn process-http-routes
  "Process HTTP endpoints and return route configurations"
  [spec]
  (for [[[host path method tag] endpoints]
        (group-by (juxt :host :path #(generate-method (:method %)) :tag)
                  (remove nil? (map :endpoint (filter #(contains? % :endpoint) spec))))]
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
         :handler (generic-reitit-handler response nil)}}])))

(defn create-ws-handler
  "Create WebSocket handler function with connection and message handling"
  [path on-connect on-message]
  (fn [request]
    (http-kit/as-channel
     request
     {:on-open (fn [channel]
                 (when on-connect
                   (handle-ws-connect channel on-connect
                                      (build-parameters
                                       {:query (:query-params request)
                                        :path (:path-params request)}))))

      :on-receive (fn [channel message]
                    (when on-message
                      (let [params (build-parameters
                                    {:query (:query-params request)
                                     :path (:path-params request)
                                     :body message})]
                        (process-ws-message channel message on-message params))))

      :on-close (fn [_channel status]
                  (log/log :info :websocket-closed
                           :path path
                           :status status))

      :on-error (fn [_channel error]
                  (log/log :error :websocket-error
                           :path path
                           :message (.getMessage error)))})))

(defn process-ws-routes
  "Process WebSocket endpoints and return route configurations"
  [spec]
  (for [route-config (filter #(contains? % :websocket) spec)]
    (let [ws-config (:websocket route-config)
          path (:path ws-config)
          on-connect (:on-connect ws-config)
          on-message (:on-message ws-config)]
      [path
       {:get
        {:no-doc true
         :handler (create-ws-handler path on-connect on-message)}}])))

(defn create-swagger-route
  "Create Swagger documentation endpoint"
  []
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "moclojer-mock"
                            :description "my mock"}}
           :handler (swagger/create-swagger-handler)}}]])

(defn merge-routes
  "Merge route definitions with the same path"
  [routes [route-name route-definition]]
  (let [?existing-definitions (get routes route-name)]
    (assoc routes route-name
           (merge ?existing-definitions route-definition))))

(defn ->reitit
  "Adapts given moclojer endpoints to reitit's data based routes, while
   parsing request data types throughout the way. Supports both HTTP endpoints
   and WebSocket endpoints."
  [spec]
  (let [http-routes (process-http-routes spec)
        ws-routes (process-ws-routes spec)
        swagger-route (create-swagger-route)
        all-routes (concat http-routes ws-routes swagger-route)]
    (->> all-routes
         (remove nil?)
         (reduce merge-routes {})
         (map identity)
         (vec))))
