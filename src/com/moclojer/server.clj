(ns com.moclojer.server
  (:require [com.moclojer.adapters :as adapters]
            [com.moclojer.config :as config]
            [com.moclojer.io-utils :refer [open-file]]
            [com.moclojer.log :as log]
            [com.moclojer.watcher :refer [start-watcher]]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.jetty])
  (:import (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (org.eclipse.jetty.servlet ServletContextHandler)))

(defn context-configurator
  "http container options, active gzip"
  [^ServletContextHandler context]
  (let [gzip-handler (GzipHandler.)]
    (.addIncludedMethods gzip-handler (make-array String 0))
    (.setExcludedAgentPatterns gzip-handler (make-array String 0))
    (.setGzipHandler context gzip-handler))
  context)

(defn get-interceptors
  "get pedestal default interceptors"
  [service-map]
  (-> service-map
      http/default-interceptors
      (update ::http/interceptors into [http/json-body
                                        (body-params/body-params)])))

(defn start-server!
  "start moclojer server"
  [*router & {:keys [start?] :or {start? true}}]
  (let [http-host (or (System/getenv "HOST") "0.0.0.0")
        http-port (or (some-> (System/getenv "PORT")
                              Integer/parseInt)
                      8000)
        http-start (if start? http/start ::http/service-fn)]
    (log/log :info
             :moclojer-start
             "-> moclojer"
             :start-server
             :host http-host
             :port http-port
             :url (str "http://" http-host ":" http-port)
             :version config/version)
    (-> {:env                     :prod
         ::http/routes            (fn [] @*router)
         ::http/secure-headers    {:content-security-policy-settings
                                   {:default-src "'self'"
                                    :style-src "'self' 'unsafe-inline'"
                                    :script-src "'self' 'unsafe-inline'"
                                    :img-src "'self' 'unsafe-inline' data: https://validator.swagger.io"}}
         ::http/type              :jetty
         ::http/join?             true
         ;; pedestal default behavior is to return 403 for invalid origins and
         ;; return Access-Control-Allow-Origin as nil
         ::http/allowed-origins   {:creds true
                                   :allowed-origins (constantly true)}
         ::http/container-options {:h2c?                 true
                                   :context-configurator context-configurator}
         ::http/host              http-host
         ::http/port              http-port}
        get-interceptors
        http/create-server
        http-start)))

(defn start-server-with-file-watcher!
  "start moclojer server with file watcher"
  [{:keys [config-path mocks-path]}]
  (let [*router (adapters/generate-routes (open-file config-path)
                                          :mocks-path mocks-path)]
    (start-watcher
     [config-path mocks-path]
     (fn [changed]
       (log/log :info :moclojer-reload :router changed)
       (reset! *router
               (adapters/generate-routes (open-file config-path)
                                         :mocks-path mocks-path))))
    (start-server! *router)))
