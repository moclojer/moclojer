(ns com.moclojer.external-body.core
  (:require
   [cheshire.core :as cheshire]
   [com.moclojer.external-body.excel :as xlsx]))

(defn ->str
  "If not already a string, parses `body` to a stringified json of `body`."
  [body]
  (if (string? body) body (cheshire/generate-string body)))

(defn type-identification
  "Identifies `external-body`'s file format type. Currently supports
  only `json` and `xlsx`."
  [external-body]
  (let [path (:path external-body)
        body (case (:provider external-body)
               "json" (slurp path)
               "xlsx" (xlsx/->map (:path external-body) (:sheet-name external-body))
               "format not supported, read documentation")]
    (->str body)))
