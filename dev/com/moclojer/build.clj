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

(def pom-template
  [[:description "Simple and efficient HTTP mock server with specification written in `yaml`, `edn` or `OpenAPI`."]
   [:url "https://moclojer.com"]
   [:licenses
    [:license
     [:name "MIT License"]
     [:url "https://opensource.org/licenses/MIT"]]]
   [:scm
    [:url "https://github.com/moclojer/moclojer"]
    [:connection "scm:git:https://github.com/moclojer/moclojer.git"]
    [:developerConnection "scm:git:ssh:git@github.com:moclojer/moclojer.git"]
    [:tag (str "v" config/version)]]])

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
     :target     "target"
     :src-dirs   (:paths basis)
     :pom-data   pom-template
     :exclude    ["docs/*" "META-INF/*" "test/*" "target/*"]}))

(defn -main
  [& args]
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

    ;; build `.jar` used uberjar (distribute software) or jar (distribute library)
    (println "args" args)
    (let [uberjar-flag (some #(= % "--uberjar") args)]
      (if uberjar-flag
        (do
          (println "Building uberjar")
          (b/uber options))
        (do
          (println "Packaging classes into jar")
          (b/jar options))))

    ;; prepare file for native image
    ;; TODO: commented feature, see why https://github.com/moclojer/moclojer/issues/158
    (native-image/prepare-files)))
