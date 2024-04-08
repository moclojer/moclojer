(ns com.moclojer.build
  (:refer-clojure :exclude [test])
  (:require [clojure.string :as string]
            [clojure.tools.build.api :as b]
            [com.moclojer.config :as config]
            [com.moclojer.native-image :as native-image]))

(def class-dir "target/classes")
(def jar-file "target/moclojer.jar")

(set! *warn-on-reflection* true)

(defmacro with-err-str
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body
       (str s#))))

(def options
  (let [basis (b/create-basis {:project "deps.edn"})]
    {:class-dir  class-dir
     :lib        'com.moclojer/moclojer
     :main       'com.moclojer.core
     :version    config/version
     :basis      basis
     :ns-compile '[com.moclojer.core]
     :uber-file  jar-file
     :jar-file   jar-file
     :src-dirs   (:paths basis)
     :exclude    ["docs/*" "META-INF/*" "test/*" "target/*"]}))

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (println "Clearing target directory")
    (b/delete {:path "target"})

    (println "Writing pom")
    (->> (b/write-pom options)
         with-err-str
         string/split-lines
         ;; Avoid confusing future me/you: suppress "Skipping coordinate" messages for our jars, we don't care, we are creating an uberjar
         (remove #(re-matches #"^Skipping coordinate: \{:local/root .*target/(lib1|lib2|graal-build-time).jar.*" %))
         (run! println))
    (b/copy-dir {:src-dirs (:paths basis)
                 :target-dir class-dir})

    (println "Compile sources to classes")
    (b/compile-clj options)

    (println "Packaging classes into jar")
    (b/jar options)

    ;; (println "Building uberjar")
    ;; (b/uber options)

    ;; prepare file for native image
    ;; TODO: commented feature, see why https://github.com/moclojer/moclojer/issues/158
    (native-image/prepare-files)))
