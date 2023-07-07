(ns moclojer.core
  (:gen-class)
  (:require [babashka.cli :as cli]
            [clojure.java.io :as io]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.jetty]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.log :as log]
            [moclojer.adapters :as adapters]
            [moclojer.config :as config]
            [moclojer.io-utils :refer [open-file]]
            [moclojer.router :as router]
            [moclojer.watcher :refer [start-watcher]])
  (:import (java.util Properties)
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

(def *pom-info
  "pom file info load"
  (delay
    (let [p (Properties.)]
      (some-> "META-INF/maven/moclojer/moclojer/pom.properties"
              io/resource
              io/reader
              (->> (.load p)))
      p)))

(defn get-interceptors [service-map]
  (-> service-map
      http/default-interceptors
      (update ::http/interceptors into [http/json-body
                                        (body-params/body-params)])))

(defn start
  "start moclojer server"
  [{:keys [current-version config-path mocks-path]}]
  (log/info
   "-> moclojer"
   :start-server
   :version current-version
   :config-path config-path
   :mocks-path mocks-path)
  (let [generate-routes (fn [config-path mocks-path]
                          (router/smart-router {::router/config (open-file config-path)
                                                ::router/mocks  (open-file mocks-path)}))
        *router (atom (generate-routes config-path mocks-path))
        get-routes (fn [] @*router)]
    (start-watcher
     [config-path mocks-path]
     (fn [changed]
       (log/info :changed changed)
       (reset! *router (generate-routes config-path mocks-path))))
    (-> {:env                     :prod
         ::http/routes            get-routes
         ::http/type              :jetty
         ::http/join?             true
         ::http/container-options {:h2c?                 true
                                   :context-configurator context-configurator}
         ::http/host              (or (System/getenv "HOST") "0.0.0.0")
         ::http/port              (or (some-> (System/getenv "PORT")
                                              Integer/parseInt)
                                      8000)}
        get-interceptors
        http/create-server
        http/start)))

(def spec {:config {:ref     "<file>"
                    :desc    "Config path <file> or the CONFIG enviroment variable."
                    :alias   :c
                    :default "moclojer.yml"}
           :mocks  {:ref     "<file>"
                    :desc    "OpenAPI v3 mocks path <file> or the MOCKS enviroment variable."
                    :alias   :m}
           :version {:desc   "Show version."
                     :alias  :v}
           :help    {:desc   "Show this Help."
                     :alias  :h}})

(defn -main
  {:org.babashka/cli {:collect {:args []}}}
  [& args]
  (let [args-opts (cli/parse-args args {:spec spec})
        envs {:config (or (System/getenv "CONFIG")
                          (config/with-xdg "moclojer.yml"))
              :mocks (System/getenv "MOCKS")}
        current-version (or (get @*pom-info "version") "dev")
        config (adapters/inputs->config args-opts envs current-version)]

    (when (:version config)
      (println "moclojer" current-version)
      (System/exit 0))

    (when (:help config)
      (println
       (str "moclojer (" current-version "), simple and efficient HTTP mock server.\r\n"
            (cli/format-opts {:spec spec :order [:config :mocks :version :help]})))
      (System/exit 0))

    (start config)))
