(ns moclojer.build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.shell :as sh]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def lib 'moclojer/moclojer)
(def class-dir "target/classes")
(def uber-file "target/moclojer.jar")

(defn sh!
  [& vs]
  (try
    (let [{:keys [exit out err]} (apply sh/sh vs)]
      (println out)
      (binding [*out* *err*]
        (println err))
      (when-not (== exit 0)
        (throw (ex-info (str "Can't " (first vs))
                        {:vs vs :exit exit}))))
    (catch Throwable ex
      (throw (ex-info (str (pr-str (first vs))
                           " do not exits")
                      {:vs vs}
                      ex)))))

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
    (b/uber {:class-dir class-dir
             :main      'moclojer.core
             :uber-file uber-file
             :basis     basis})
    ;; native-image
    #_(sh! "./bin/gu" "install" "native-image"
           :dir (io/file (System/getProperty "java.home")))
    #_(sh! (str (io/file (System/getProperty "java.home") "bin" "native-image"))
           "-cp" (str (string/join ":" (:classpath-roots basis)) ":target/classes")
           "-H:Name=moclojer"
           "-H:+ReportExceptionStackTraces"
           "--initialize-at-build-time"
           "--verbose"
           "--no-fallback"
           "--no-server"
           "--allow-incomplete-classpath"
           "-H:ReflectionConfigurationFiles=reflect-config.json"
           "moclojer.core")))
