(ns com.moclojer.io-utils
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [com.moclojer.log :as log]
            [yaml.core :as yaml])
  (:import [java.io FileNotFoundException]))

(defn open-file [path]
  (if (empty? path)
    (log/log :error :open-config :not-found "file not found")
    (try
      (if (string/ends-with? path ".edn")
        (edn/read-string (str "[" (slurp path) "]"))
        (yaml/from-file path))
      (catch FileNotFoundException e
        (log/log :error :open-config :exception (str "file not found" e))))))
