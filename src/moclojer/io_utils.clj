(ns moclojer.io-utils
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]
   [moclojer.log :as logs]
   [yaml.core :as yaml])
  (:import
   [java.io FileNotFoundException]))

(defn open-file [path]
  (if (empty? path)
    (logs/log :error :open-config :not-found "file not found")
    (try
      (if (string/ends-with? path ".edn")
        (edn/read-string (str "[" (slurp path) "]"))
        (yaml/from-file path))
      (catch FileNotFoundException e
        (logs/log :error :open-config :exception (str "file not found" e))))))
