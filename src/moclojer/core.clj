(ns moclojer.core
  (:gen-class)
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty]
            [io.pedestal.http.route :as route]
            [io.pedestal.log :as log]
            [moclojer.router :as router]
            [clojure.spec.alpha :as s])
  (:import (java.util.jar Manifest)
           (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (org.eclipse.jetty.servlet ServletContextHandler)))

(def moclojer-version
  (some-> "META-INF/MANIFEST.MF"
    io/resource
    io/input-stream
    Manifest.
    .getMainAttributes
    (.getValue "Implementation-Version")))

(defn context-configurator
  [^ServletContextHandler context]
  (let [gzip-handler (GzipHandler.)]
    (.addIncludedMethods gzip-handler (make-array String 0))
    (.setExcludedAgentPatterns gzip-handler (make-array String 0))
    (.setGzipHandler context gzip-handler))
  context)

#_(defn watch-service
    []
    ;; TODO: Not working. Firing only once.
    (let [*router (atom (router/make-smart-router))
          p (java.nio.file.Paths/get "." (into-array String []))
          ws (.newWatchService (java.nio.file.FileSystems/getDefault))]
      (.register p ws (into-array [java.nio.file.StandardWatchEventKinds/ENTRY_MODIFY
                                   java.nio.file.StandardWatchEventKinds/ENTRY_CREATE]))
      (async/thread
        (try
          (loop []
            (prn :waiting)
            (let [key (.take ws)]
              (prn :refresh)
              (reset! *router (router/make-smart-router))
              (log/info {:msg "refresh routes"})
              (prn :ok))
            (recur))
          (catch Throwable ex
            (println ex))))))

(defn -main
  "start moclojer server"
  [& _]
  (prn (list '-> 'moclojer :start-server :version moclojer-version))
  (let [config (System/getenv "CONFIG")
        mocks (System/getenv "MOCKS")
        env {::router/config (or config "moclojer.yml")
             ::router/mocks  mocks}
        *last-ex-msg (atom nil)
        try-routes (fn [old-routes]
                     (try
                       (router/make-smart-router env)
                       (catch Throwable ex
                         (when-not (= (ex-message ex)
                                     @*last-ex-msg)
                           (log/error :exception ex)
                           (reset! *last-ex-msg (ex-message ex)))
                         old-routes)))
        *router (atom (try-routes (route/expand-routes #{})))]
    ;; TODO: Use watch-service
    (async/thread
      (loop []
        (let [wait (async/timeout 1000)]
          (swap! *router try-routes)
          (async/<!! wait))
        (recur)))
    (-> {:env                     :prod
         ::http/routes            (fn []
                                    @*router)
         ::http/type              :jetty
         ::http/join?             true
         ::http/container-options {:h2c?                 true
                                   :context-configurator context-configurator}
         ::http/port              (or (some-> (System/getenv "PORT")
                                        Integer/parseInt)
                                    8000)}
        http/default-interceptors
        (update ::http/interceptors into [http/json-body])
        http/create-server
        http/start)))
