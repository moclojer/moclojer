(ns com.moclojer.server
  (:require
   [clojure.string :as str]
   [com.moclojer.adapters :as adapters]
   [com.moclojer.config :as config]
   [com.moclojer.io-utils :refer [open-file]]
   [com.moclojer.log :as log]
   [com.moclojer.watcher :refer [start-watch]]
   [com.moclojer.middleware.rate-limit :as rate-limit]
   [com.moclojer.middleware.latency :as latency]
   [com.moclojer.middleware.chaos :as chaos]
   [muuntaja.core :as m]
   [reitit.coercion.malli :as reitit-malli]
   [reitit.coercion.spec]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [ring.adapter.jetty :as jetty]))

(defn host-middleware
  [handler-fn]
  (fn [request]
    (handler-fn
     (let [server-host (get-in request [:reitit.core/match :data :host])
           header-host (get-in request [:headers "host"] "localhost")
           host (if (str/includes? header-host ":")
                  (first (str/split header-host #":"))
                  header-host)]
       (if (or (nil? server-host) (= host server-host))
         request
         (throw (ex-info "Invalid host"
                         {:status 403
                          :value host
                          :expected server-host})))))))

(defn reitit-router [*router]
  (ring/ring-handler
   (ring/router
    @*router
    {:exception pretty/exception
     :data {:coercion reitit-malli/coercion
            :muuntaja (m/create
                       (-> (assoc-in
                            m/default-options
                            [:formats "application/json"
                             :decoder-opts :bigdecimals]
                            true)
                           (assoc :default-format "application/json")))
            :middleware [host-middleware
                         log/log-request-middleware
                         swagger/swagger-feature
                         rate-limit/wrap-rate-limit
                         latency/wrap-latency
                         parameters/parameters-middleware
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         muuntaja/format-request-middleware
                         coercion/coerce-request-middleware
                         coercion/coerce-response-middleware
                         multipart/multipart-middleware
                         chaos/wrap-chaos]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/docs"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/create-resource-handler)
    (ring/create-default-handler))))

(defn start-server!
  [*router & {:keys [join?]
              :or {join? true}}]
  (let [http-host (or (System/getenv "HOST") "0.0.0.0")
        http-port (or (some-> (System/getenv "PORT")
                              Integer/parseInt)
                      8000)]

    (log/log :info
             :moclojer-start
             "-> moclojer"
             :start-server
             :host http-host
             :port http-port
             :url (str "http://" http-host ":" http-port)
             :version config/version)

    (jetty/run-jetty (reitit-router *router)
                     {:port http-port
                      :join? join?})))

(defn create-watcher
  "Creates a file watcher that monitors changes in the configuration and mocks files.
  When changes are detected, it automatically rebuilds and updates the router.

  Parameters:
  - *router: An atom containing the current router configuration.
  - config-path: The path to the main configuration file.
  - mocks-path: The path to the directory containing mock files.

  Returns:
  A watcher object that can be used to stop the watching process if needed.

  Side effects:
  - Sets up file watchers for both config and mocks files.
  - Automatically updates the router when changes are detected."
  [*router & {:keys [config-path mocks-path]}]
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
  "Starts the moclojer server with an integrated file watcher.

  The file watcher monitors changes in the configuration and mocks files,
  automatically updating the server's behavior when changes are detected.

  Side effects:
    - Starts the server
    - Sets up file watchers for both config and mocks files.
    - Automatically updates the server configuration when file changes are detected."
  [{:keys [config-path mocks-path]}]
  (let [*router (adapters/generate-routes (open-file config-path)
                                          :mocks-path mocks-path)]
    (create-watcher *router {:config-path config-path :mocks-path mocks-path})
    (start-server! *router)))
