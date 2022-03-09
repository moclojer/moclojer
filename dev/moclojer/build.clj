(ns moclojer.build
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.build.api :as b])
  (:import (java.lang ProcessBuilder$Redirect)
           (java.net.http HttpClient HttpResponse HttpRequest HttpResponse$BodyHandlers)
           (java.net URI ConnectException)
           (java.nio.channels ClosedChannelException)))

(def lib 'moclojer/moclojer)
(def class-dir "target/classes")
(def uber-file "target/moclojer.jar")
(set! *warn-on-reflection* true)
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
             :basis     basis})
    (.mkdirs (io/file "target" "native"))
    (spit (io/file "target" "native" "filter.json")
          (json/write-str {}))))
