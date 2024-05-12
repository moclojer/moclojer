(ns com.moclojer.server
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]
   [com.moclojer.adapters :as adapters]
   [com.moclojer.config :as config]
   [com.moclojer.io-utils :refer [open-file]]
   [com.moclojer.log :as log]
   [com.moclojer.watcher :refer [start-watch]]
   [io.pedestal.http :as http]
   [io.pedestal.http.body-params :as body-params]
   [io.pedestal.http.jetty]
   [io.pedestal.interceptor.error :refer [error-dispatch]]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.dev.pretty :as pretty]
   [reitit.http :as r-http]
   [reitit.http.coercion :as coercion]
   [reitit.http.interceptors.exception :as exception]
   [reitit.http.interceptors.multipart :as multipart]
   [reitit.http.interceptors.muuntaja :as muuntaja]
   [reitit.http.interceptors.parameters :as parameters]
   [reitit.pedestal :as pedestal]
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui])
  (:import
   (org.eclipse.jetty.server.handler.gzip GzipHandler)
   (org.eclipse.jetty.servlet ServletContextHandler)))

(defn context-configurator
  "http container options, active gzip"
  [^ServletContextHandler context]
  (let [gzip-handler (GzipHandler.)]
    (.addIncludedMethods gzip-handler (make-array String 0))
    (.setExcludedAgentPatterns gzip-handler (make-array String 0))
    (.setGzipHandler context gzip-handler))
  context)

#_:clj-kondo/ignore
(def interceptor-error-handler
  "capture and format in json exception Internal Server Error"
  (error-dispatch [context error]
                  :else
                  (assoc context :response {:status 500
                                            :headers {"Content-type" "application/json"}
                                            :body (->> error .toString (hash-map :error) json/write-str)})))

(defn get-interceptors
  "get pedestal default interceptors"
  [service-map]
  (-> service-map
      http/default-interceptors
      (update ::http/interceptors into [http/not-found
                                        http/json-body
                                        (body-params/body-params)
                                        interceptor-error-handler])))

(defn build-config-map
  "build pedestal config map"
  [*router & {:keys [http-host http-port join?]}]
  {:env                     config/moclojer-environment
   ::http/request-logger    log/request
   ::http/routes            (fn [] @*router)
   ::http/type              :jetty
   ::http/join?              join?
   ::http/allowed-origins   {:creds true
                             :allowed-origins (constantly true)}
   ::http/container-options {:h2c?                 true
                             :context-configurator context-configurator}
   ::http/host              http-host
   ::http/port              http-port})

(defn reitit-router [*router]
  (-> (pedestal/routing-interceptor
       (r-http/router
        @*router
        {;:reitit.interceptor/transform dev/print-context-diffs ;; pretty context diffs
                  ;;:validate spec/validate ;; enable spec validation for route data
                  ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
         :exception pretty/exception
         :data {:coercion reitit.coercion.spec/coercion
                :muuntaja m/instance
                :interceptors [;; swagger feature
                               swagger/swagger-feature
                             ;; query-params & form-params
                               (parameters/parameters-interceptor)
                             ;; content-negotiation
                               (muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                               (muuntaja/format-response-interceptor)
                             ;; exception handling
                               (exception/exception-interceptor)
                             ;; decoding request body
                               (muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                               (coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                               (coercion/coerce-request-interceptor)
                             ;; multipart
                               (multipart/multipart-interceptor)]}})
       (ring/routes
        (swagger-ui/create-swagger-ui-handler
         {:path "/docs"
          :config {:validatorUrl nil
                   :operationsSorter "alpha"}})
        (ring/create-resource-handler)
        (ring/create-default-handler)))))

(defn start-server!
  "start moclojer server"
  [*router & {:keys [start?
                     join?] :or {start? true
                                 join? true}}]
  (let [http-host (or (System/getenv "HOST") "0.0.0.0")
        http-port (or (some-> (System/getenv "PORT")
                              Integer/parseInt)
                      8000)
        http-start (if start? http/start ::http/service-fn)
        swagger? (or (System/getenv "SWAGGER") false)]
    (log/log :info
             :moclojer-start
             "-> moclojer"
             :start-server
             :host http-host
             :port http-port
             :url (str "http://" http-host ":" http-port)
             :version config/version)
    (if swagger?
      (let [router (reitit-router *router)]
        (-> {:env                     config/moclojer-environment
             ::http/request-logger    log/request
             ::http/routes            []
             ::http/type              :jetty
             ::http/join?              join?
             ::http/container-options {:h2c?                 true
                                       :context-configurator context-configurator}
 ;; allow serving the swagger-ui styles & scripts from self
             ::http/secure-headers {:content-security-policy-settings
                                    {:default-src "'self'"
                                     :style-src "'self' 'unsafe-inline'"
                                     :script-src "'self' 'unsafe-inline'"}}
             ::http/host              http-host
             ::http/port              http-port}

            (http/default-interceptors)
            (pedestal/replace-last-interceptor router)

            (http/dev-interceptors)
            (http/create-server)
            http-start))

      (->
       *router
       (build-config-map  {:http-host http-host
                           :http-port http-port
                           :join? join?})
       get-interceptors
       http/create-server
       http-start))))

(defn create-watcher [*router & {:keys [config-path mocks-path]}]
  (start-watch [{:file config-path
                 :event-types [:create :modify :delete]
                 :callback (fn [_event file]
                             (when (and (= file config-path)
                                        (not (nil? config-path)))
                               (log/log :info :moclojer-reload :router file :config config-path)
                               (reset! *router
                                       @(adapters/generate-routes (open-file config-path)
                                                                  :mocks-path mocks-path))))}
                {:file mocks-path
                 :callback (fn [_event file]
                             (when (and (= file mocks-path)
                                        (not (nil? mocks-path)))
                               (log/log :info :moclojer-reload :router file :mock-path mocks-path)
                               (reset! *router
                                       @(adapters/generate-routes (open-file config-path)
                                                                  :mocks-path mocks-path))))
                 :event-types [:create :modify :delete]}]))

(defn start-server-with-file-watcher!
  "start moclojer server with file watcher"
  [{:keys [config-path mocks-path]}]
  (let [*router (adapters/generate-routes (open-file config-path)
                                          :mocks-path mocks-path)]
    (clojure.pprint/pprint *router)
    (create-watcher *router {:config-path config-path :mocks-path mocks-path})
    (start-server! *router)))
