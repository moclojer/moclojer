(ns moclojer.build
  (:require [clojure.tools.build.api :as b]))

(def lib 'moclojer/moclojer)
(def class-dir "target/classes")
(def uber-file "target/moclojer.jar")

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
    (b/uber {:class-dir class-dir
             :main      'moclojer.core
             :uber-file uber-file
             :manifest  (merge {}
                               (when-let [sha (System/getenv "CI_COMMIT_SHA")]
                                 {"SCM-Revision" sha}))
             :basis     basis})))
