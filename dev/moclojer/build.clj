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

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/delete {:path "target"})
    (b/write-pom {:class-dir class-dir
                  :lib       'moclojer/moclojer
                  :version   config/version
                  :basis     basis
                  :src-dirs  (:paths basis)})
    (b/compile-clj {:basis     basis
                    :src-dirs  (:paths basis)
                    :class-dir class-dir})
    (b/uber {:class-dir class-dir
             :main      'moclojer.core
             :uber-file uber-file
             :basis     basis})

    (.mkdirs (io/file "target" "native"))

    ;; create native-image configuration file `filter.json`
    (spit (io/file "target" "native" "filter.json")
          (json/write-str {:rules []}))

    ;; create native-image parameter file `@native-image-args`
    (spit (io/file "target" "native" "native-image-args")
          (string/join "\n" ["-H:Name=moclojer"
                             "-Dio.pedestal.log.defaultMetricsRecorder=nil"
                             "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"
                             "-Dorg.slf4j.simpleLogger.log.org.eclipse.jetty.server=error"
                             "--allow-incomplete-classpath"

                             ;; TODO: Option 'EnableAllSecurityServices' is deprecated
                             "--enable-all-security-services"
                             "--features=clj_easy.graal_build_time.InitClojureClasses"

                             ;; lists managed in the native-image library
                             native-image/initialize-at-build-time
                             native-image/initialize-at-run-time
                             native-image/trace-class-initialization

                             ;; "-H:+PrintClassInitialization"
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
