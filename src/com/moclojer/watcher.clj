(ns com.moclojer.watcher
  (:require
   [clojure.java.io :as io])
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
  [{:keys [path event-types
           callback]}
   watcher
   keys]
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
                    (catch Exception e))

        modifiers (when modifier
                    (doto (make-array java.nio.file.WatchEvent$Modifier 1)
                      (aset 0 modifier)))

        key (if modifiers
              (.register dir watcher (into-array types) modifiers)
              (.register dir watcher (into-array types)))]

    (assoc keys key [dir callback])))

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
  [watcher keys]
  (let [key (.take watcher)
        [dir callback] (keys key)]
    (do
      (doseq [event (.pollEvents key)]
        (let [kind (kind-to-key (.. event kind name))
              name (->> event
                        .context
                        (.resolve dir)
                        str)]
          ; Run callback in another thread
          (future (do
                    (callback kind name)
                    (.reset key)))))
      (recur watcher keys))))

(defn start-watch [specs]
  (let [specs (-> (filter-nil-spec specs) add-path)
        watcher (.. FileSystems getDefault newWatchService)
        keys (reduce (fn [keys spec]
                       (register spec watcher keys)) {} specs)]
    (letfn [(close-watcher []
              (.close watcher))]
      (future (watch watcher keys))
      close-watcher)))

(comment
  (start-watch [{:file "/Users/matheus.machado/.config/moclojer.yaml"
                 :event-types [:create :modify :delete]
                 :callback (fn [event filename] (prn event filename))}])

  ;
  )
