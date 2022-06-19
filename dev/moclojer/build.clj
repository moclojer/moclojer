(ns moclojer.build
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.build.api :as b]))

(def lib 'moclojer/moclojer)
(def class-dir "target/classes")
(def uber-file "target/moclojer.jar")
(def moclojer-version
  "moclojer version rendering constant"
  "0.1")

(set! *warn-on-reflection* true)

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/delete {:path "target"})
    (b/write-pom {:class-dir class-dir
                  :lib       lib
                  :version   moclojer-version
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
    (spit (io/file "target" "native" "filter.json")
          (json/write-str {}))
    (spit (io/file "target" "native" "native-image-args")
          (string/join "\n" ["-H:Name=moclojer"
                             "-Dio.pedestal.log.defaultMetricsRecorder=nil"
                             "-H:+ReportExceptionStackTraces"
                             "--allow-incomplete-classpath"
                             "--initialize-at-build-time"
                             "--verbose"
                             "-H:+DashboardHeap"
                             "-H:+DashboardCode"
                             "-H:+DashboardBgv"
                             "-H:+DashboardJson"
                             "--no-fallback"]))))
