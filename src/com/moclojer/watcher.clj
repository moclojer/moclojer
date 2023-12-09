(ns com.moclojer.watcher
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io])
  (:import (java.nio.file
            FileSystems
            Path
            StandardWatchEventKinds
            WatchEvent)
           (java.util.concurrent TimeUnit)))

(defn start-watcher
  "watch spec file change to server reload
   used async/thread to not have time for server shutdown"
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
