(ns moclojer.io-utils
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.pedestal.log :as log]
            [yaml.core :as yaml])
  (:import [java.io FileNotFoundException]))

(defn open-file [path]
  (when (seq path)
    (try
      (if (string/ends-with? path ".edn")
        (edn/read-string (str "[" (slurp path) "]"))
        (yaml/from-file path))
      (catch FileNotFoundException e
        (log/error :open-config (str "file not found" e))))))
