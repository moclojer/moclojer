(ns moclojer.build
  (:require [clojure.string :as string]
            [clojure.tools.build.api :as b]
            [moclojer.config :as config]
            [moclojer.native-image :as native-image]))

(def class-dir "target/classes")
(def uber-file "target/moclojer.jar")

(set! *warn-on-reflection* true)

(defmacro with-err-str
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body
       (str s#))))

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (println "Clearing target directory")
    (b/delete {:path "target"})

    (println "Writing pom")
    (->> (b/write-pom {:class-dir class-dir
                       :lib       'moclojer/moclojer
                       :version   config/version
                       :basis     basis
                       :src-dirs  (:paths basis)})
         with-err-str
         string/split-lines
         ;; Avoid confusing future me/you: suppress "Skipping coordinate" messages for our jars, we don't care, we are creating an uberjar
         (remove #(re-matches #"^Skipping coordinate: \{:local/root .*target/(lib1|lib2|graal-build-time).jar.*" %))
         (run! println))
    (b/copy-dir {:src-dirs (:paths basis)
                 :target-dir class-dir})

    (println "Compile sources to classes")
    (b/compile-clj {:basis      basis
                    :src-dirs   (:paths basis)
                    :class-dir  class-dir
                    :ns-compile '[moclojer.core]})

    (println "Building uberjar")
    (b/uber {:class-dir class-dir
             :main      'moclojer.core
             :uber-file uber-file
             :basis     basis})

    ;; prepare file for native image
    ;; TODO: commented feature, see why https://github.com/moclojer/moclojer/issues/158
    (native-image/prepare-files)))
