(ns moclojer.build
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
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

    (println "Building native image configuration files")
    ;; create native-image configuration file `filter.json`
    (spit (io/file "target" "filter.json")
          (json/write-str {:rules []}))

    ;; create native-image parameter file `@native-image-args`
    (spit (io/file "target" "native-image-args")
          (string/join "\n" ["-H:Name=moclojer"
                             "-Dio.pedestal.log.defaultMetricsRecorder=nil"
                             "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"
                             "-Dorg.slf4j.simpleLogger.log.org.eclipse.jetty.server=error"
                             "--allow-incomplete-classpath"

                             ;; TODO: Option 'EnableAllSecurityServices' is deprecated
                             "--enable-all-security-services"

                             ;; TODO: use clj-easy.graal-build-time
                             ;; "--features=clj_easy.graal_build_time.InitClojureClasses"

                             ;; lists managed in the native-image library
                             native-image/initialize-at-build-time

                             "-H:DashboardDump=report/moclojer"
                             "-H:+ReportExceptionStackTraces"
                             "-H:+DashboardHeap"
                             "-H:+DashboardCode"
                             "-H:+DashboardBgv"
                             "-H:+DashboardJson"
                             "-H:ReflectionConfigurationFiles=reflect-config.json"
                             "-H:ResourceConfigurationFiles=resource-config.json"
                             "--no-fallback"
                             "--verbose"]))))
