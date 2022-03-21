(ns moclojer.core
  (:gen-class)
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty]
            [io.pedestal.log :as log]
            [moclojer.helper :as helper]
            [moclojer.router :as router])
  (:import (java.nio.file FileSystems Path StandardWatchEventKinds WatchEvent)
           (java.util.concurrent TimeUnit)
           (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (org.eclipse.jetty.servlet ServletContextHandler)))
(set! *warn-on-reflection* true)
(defn context-configurator
  [^ServletContextHandler context]
  (let [gzip-handler (GzipHandler.)]
    (.addIncludedMethods gzip-handler (make-array String 0))
    (.setExcludedAgentPatterns gzip-handler (make-array String 0))
    (.setGzipHandler context gzip-handler))
  context)

(defn watch-service
  [files on-change]
  (let [ws (.newWatchService (FileSystems/getDefault))
        kv-paths (keep (fn [x]
                         (when-let [path (some-> x
                                           io/file
                                           .toPath
                                           .toAbsolutePath)]
                           [x path]))
                   files)
        roots (into #{}
                (keep (fn [[_ ^Path v]]
                        (.getParent v)))
                kv-paths)]
    (doseq [path roots]
      (.register ^Path path
        ws (into-array [StandardWatchEventKinds/ENTRY_MODIFY
                        StandardWatchEventKinds/OVERFLOW
                        StandardWatchEventKinds/ENTRY_DELETE
                        StandardWatchEventKinds/ENTRY_CREATE])))
    (async/thread
      (loop []
        (when-let [watch-key (.poll ws 1 TimeUnit/SECONDS)]
          (let [changed (keep (fn [^WatchEvent event]
                                (let [^Path changed-path (.context event)]
                                  (first (for [[k v] kv-paths
                                               :when (= v (.toAbsolutePath changed-path))]
                                           k))))
                          (.pollEvents watch-key))]
            (when (seq changed)
              (on-change (set changed))))
          (.reset watch-key))
        (recur)))))

(defn -main
  "start moclojer server"
  [& _]
  (prn (list '-> 'moclojer :start-server :version helper/moclojer-version))
  (let [config (System/getenv "CONFIG")
        mocks (System/getenv "MOCKS")
        env {::router/config (or config "moclojer.yml")
             ::router/mocks  mocks}
        *router (atom (router/make-smart-router
                        env))]
    (watch-service (vals env)
      (fn [changed]
        (log/info :changed changed)
        (reset! *router (router/make-smart-router env))))
    (-> {:env                     :prod
         ::http/routes            (fn [] @*router)
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
