(ns moclojer.io-utils
  (:refer-clojure :exclude [load])
  (:require [clojure.string :as string]
            [yaml.core :as yaml])
  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)
           (java.io File)))

(defn write-config
  "write configuration file with receiving data type (yaml or edn) and structure"
  [extension values]
  (let [dir (.toFile (Files/createTempDirectory "moclojer"
                                                ^"[Ljava.nio.file.attribute.FileAttribute;"
                                                (into-array FileAttribute [])))
        file (File/createTempFile "config" (str "." extension) dir)]
    (spit file
          (case extension
            "edn" (string/join (System/getProperty "line.separator")
                               (map str values))
            ("yml" "yaml") (yaml/generate-string values)))
    (.deleteOnExit file)
    (.getCanonicalPath file)))
