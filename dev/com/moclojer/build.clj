(ns com.moclojer.build
  (:refer-clojure :exclude [test])
  (:require [clojure.string :as string]
            [clojure.tools.build.api :as b]
            [com.moclojer.config :as config]
            [com.moclojer.native-image :as native-image]))

(def class-dir "target/classes")

(set! *warn-on-reflection* true)

(defmacro with-err-str
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body
       (str s#))))

(def uber-options
  (let [basis (b/create-basis {:project "deps.edn"})]
    {:class-dir  class-dir
     :lib        'com.moclojer/moclojer
     :main       'com.moclojer.core
     :version    config/version
     :basis      basis
     :ns-compile '[com.moclojer.core]
     :uber-file  "target/moclojer.jar"
     :src-dirs   (:paths basis)
     :exclude    ["docs/*" "META-INF/*" "test/*" "target/*"]}))

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (println "Clearing target directory")
    (b/delete {:path "target"})

    (println "Writing pom")
    (->> (b/write-pom uber-options)
         with-err-str
         string/split-lines
         ;; Avoid confusing future me/you: suppress "Skipping coordinate" messages for our jars, we don't care, we are creating an uberjar
         (remove #(re-matches #"^Skipping coordinate: \{:local/root .*target/(lib1|lib2|graal-build-time).jar.*" %))
         (run! println))
    (b/copy-dir {:src-dirs (:paths basis)
                 :target-dir class-dir})

    (println "Compile sources to classes")
    (b/compile-clj uber-options)

    (println "Building uberjar")
    (b/uber uber-options)

    ;; prepare file for native image
    ;; TODO: commented feature, see why https://github.com/moclojer/moclojer/issues/158
    (native-image/prepare-files)))
