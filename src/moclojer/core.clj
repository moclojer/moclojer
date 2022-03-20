(ns moclojer.core
  (:gen-class)
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty]
            [moclojer.helper :as helper]
            [moclojer.router :as router])
  (:import (java.nio.file Files LinkOption)
           (java.nio.file.attribute BasicFileAttributes)
           (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (org.eclipse.jetty.servlet ServletContextHandler)))

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

(defn check-changes
  [file-state]
  (let [file-state (for [[file-name last-modified-time] file-state
                         :let [f (io/file file-name)]
                         :when f]
                     (if (.exists f)
                       [file-name
                        (.toInstant (.lastModifiedTime (Files/readAttributes
                                                        (.toPath f)
                                                        BasicFileAttributes
                                                        ^"[Ljava.nio.file.LinkOption;" (into-array LinkOption []))))
                        last-modified-time]
                       [file-name nil last-modified-time]))]

    {::file-state (into {}
                        (map (fn [kvs]
                               (vec (take 2 kvs))))
                        file-state)
     ::changed?   (boolean (some (fn [[_ new old]]
                                   (not= new old))
                                 file-state))}))

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
    ;; TODO: Use (watch-service)
    (async/thread
      (loop [file-state {(::router/config env) nil
                         (::router/mocks env)  nil}]
        (let [wait (async/timeout 1000)
              {::keys [changed? file-state]} (check-changes file-state)]
          (when changed?
            (reset! *router (router/make-smart-router env)))
          (async/<!! wait)
          (recur file-state))))
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
