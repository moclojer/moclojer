(ns moclojer.build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.shell :as sh]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def lib 'moclojer/moclojer)
(def class-dir "target/classes")
(def uber-file "target/moclojer.jar")
(def java-home (System/getProperty "java.home"))

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/delete {:path "target"})

    (b/write-pom {:class-dir class-dir
                  :lib       lib
                  :version   "1.0.0"
                  :basis     basis
                  :src-dirs  (:paths basis)})
    (b/compile-clj {:basis     basis
                    :src-dirs  (:paths basis)
                    :class-dir class-dir})
    ;; JAR
    #_(b/uber {:class-dir class-dir
               :main      'moclojer.core
               :uber-file uber-file
               :basis     basis})
    ;; native-image
    (run! println (vals (sh/sh "./bin/gu" "install" "native-image"
                               :dir (io/file java-home))))
    (run! println (vals (sh/sh (str (io/file java-home "bin" "native-image"))
                               "-cp" (str (string/join ":" (:classpath-roots (b/create-basis {:project "deps.edn"}))) ":target/classes")
                               "-H:Name=moclojer"
                               "-H:+ReportExceptionStackTraces"
                               "--initialize-at-build-time"
                               "--verbose"
                               "--no-fallback"
                               "--no-server"
                               "--allow-incomplete-classpath"
                               "-H:ReflectionConfigurationFiles=reflect-config.json"
                               "moclojer.core")))))
