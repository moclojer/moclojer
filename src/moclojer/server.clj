(ns moclojer.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.jetty]
            [moclojer.io-utils :refer [open-file]]
            [moclojer.log :as log]
            [moclojer.router :as router]
            [moclojer.watcher :refer [start-watcher]])
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

(defn get-interceptors [service-map]
  (-> service-map
      http/default-interceptors
      (update ::http/interceptors into [http/json-body
                                        (body-params/body-params)])))

(defn start
  "start moclojer server"
  [{:keys [current-version config-path mocks-path]}]

  (let [generate-routes (fn [config-path mocks-path]
                          (router/smart-router {::router/config (open-file config-path)
                                                ::router/mocks  (open-file mocks-path)}))
        http-host (or (System/getenv "HOST") "0.0.0.0")
        http-port (or (some-> (System/getenv "PORT")
                              Integer/parseInt)
                      8000)
        *router (atom (generate-routes config-path mocks-path))
        get-routes (fn [] @*router)]
    (log/log
     :info
     :moclojer-start
     "-> moclojer"
     :start-server
     :host http-host
     :port http-port
     :url (str "http://" http-host ":" http-port)
     :version current-version
     :config-path config-path
     :mocks-path mocks-path)
    (start-watcher
     [config-path mocks-path]
     (fn [changed]
       (log/log :info :reload :router changed)
       (reset! *router (generate-routes config-path mocks-path))))
    (-> {:env                     :prod
         ::http/routes            get-routes
         ::http/type              :jetty
         ::http/join?             true
         ;; pedestal default behavior is to return 403 for invalid origins and
         ;; return Access-Control-Allow-Origin as nil
         ::http/allowed-origins   {:creds true :allowed-origins (constantly true)}
         ::http/container-options {:h2c?                 true
                                   :context-configurator context-configurator}
         ::http/host              http-host
         ::http/port              http-port}
        get-interceptors
        http/create-server
        http/start)))
