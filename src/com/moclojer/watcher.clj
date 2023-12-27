(ns com.moclojer.watcher
  (:require
   [clojure.java.io :as io]
   [com.moclojer.log :as log])
  (:import
   (java.nio.file FileSystems Paths StandardWatchEventKinds)))

(def
  ^{:doc "This is a map of kw->events to register in the path"}
  kw->events
  {:create StandardWatchEventKinds/ENTRY_CREATE
   :delete  StandardWatchEventKinds/ENTRY_DELETE
   :modify  StandardWatchEventKinds/ENTRY_MODIFY
   :overflow  StandardWatchEventKinds/OVERFLOW})

(defn register
  ^{:doc "
    Register a path to be watched by the watcher and return a new map with the key and the path.

    output: {PollingWatchKey [path watcher]}"}
  [{:keys [path event-types
           callback]}
   watcher
   watch-keys]
  (let [;make-array is needed because Paths/get is a variadic method Java compiler handles 
        ;variadic method automatically, but when using Clojure it's necessary 
        ;to manually supply an array at the end.
        dir (Paths/get path (make-array String 0))
        types (reduce (fn [acc type]
                        (conj acc (kw->events type)))
                      []
                      event-types)
        modifier  (try
                    (let [c (Class/forName "com.sun.nio.file.SensitivityWatchEventModifier")
                          f (.getField c "HIGH")]
                      (.get f c))
                    (catch Exception e
                      (log/log :error :watcher-sensitivy :error e)))

        modifiers (when modifier
                    (doto (make-array java.nio.file.WatchEvent$Modifier 1)
                      (aset 0 modifier)))

        k (if modifiers
            (.register dir watcher (into-array types) modifiers)
            (.register dir watcher (into-array types)))]

    (assoc watch-keys k [dir callback])))

(defn get-parent [path]
  (-> path io/file .toPath .getParent str))

(defn add-path [specs]
  (map (fn [spec]
         (assoc spec :path (get-parent (:file spec)))) specs))

(defn filter-nil-spec [specs]
  (filter (fn [spec]
            (not (nil? (:file spec)))) specs))

(defn kind-to-key [kind]
  (case kind
    "ENTRY_CREATE" :create
    "ENTRY_MODIFY" :modify
    "ENTRY_DELETE" :delete))

(defn watch
  [watcher watch-keys]
  (let [polling-key (.take watcher)
        [dir callback] (watch-keys polling-key)]
    ^{:clj-kondo/ignore [:redundant-do]}
    (do
      (doseq [event (.pollEvents polling-key)]
        (let [kind (kind-to-key (.. event kind name))
              name (->> event
                        .context
                        (.resolve dir)
                        str)]
          ; Run callback in another thread
          ^{:clj-kondo/ignore [:redundant-do]}
          (future (do
                    (callback kind name)
                    (.reset polling-key)))))
      (recur watcher watch-keys))))

(defn start-watch [specs]
  (let [specs (-> (filter-nil-spec specs) add-path)
        watcher (.. FileSystems getDefault newWatchService)
        watch-keys (reduce (fn [ks spec]
                             (register spec watcher ks)) {} specs)]
    (prn watch-keys)
    (letfn [(close-watcher []
              (.close watcher))]
      (future (watch watcher watch-keys))
      close-watcher)))

(comment
  (def stop
    (start-watch [{:file "/Users/matheus.machado/.config/moclojer.yaml"
                   :event-types [:create :modify :delete]
                   :callback (fn [event filename] (prn event filename))}]))
  ; stop the watcher
  (stop)
  )
