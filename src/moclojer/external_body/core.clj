(ns moclojer.external-body.core
  (:require [cheshire.core :as cheshire]
            [moclojer.external-body.excel :as xlsx]))

(defn ->str
  "convert body to string, if it is edn it will be converted to json->str"
  [body]
  (if (string? body) body (cheshire/generate-string body)))

(defn type-identification
  "identify type of external body"
  [external-body]
  (let [path (:path external-body)
        body (case (:provider external-body)
               "json" (slurp path)
               "excel" (xlsx/->map (:path external-body) (:sheet-name external-body))
               "format not supported, read documentation")]
    (->str body)))
