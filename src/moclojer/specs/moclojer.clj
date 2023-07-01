(ns moclojer.specs.moclojer
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.pedestal.http.route.definition.table :as table]
            [io.pedestal.log :as log]
            [moclojer.handler :as handler]
            [yaml.core :as yaml])
  (:import (java.io FileNotFoundException)))

(defn open-config
  "open file"
  [path]
  (if (empty? path)
    (log/error :open-config "file not found")
    (try
      (if (string/ends-with? path ".edn")
        (edn/read-string (str "[" (slurp path) "]"))
        (yaml/from-file path))
      (catch FileNotFoundException e
        (log/error :open-config (str "file not found" e))))))

(defn generate-routes
  "generate route from moclojer spec"
  [endpoints]
  (log/info :mode "moclojer")
  (->>
   (for [[groups ops] (group-by (juxt :host :path :method) (remove nil? (map :endpoint endpoints)))]
     (let [host (or (nth groups 0) "localhost")
           path (nth groups 1)
           method (or (string/lower-case (nth groups 2)) "get")
           method-keyword (keyword (string/lower-case method))
           route-name (keyword (str method "-"
                                    host "-"
                                    (string/replace
                                     (string/replace path "/" "")
                                     ":" "--")))
           response (:response (first ops))]
       [path
        method-keyword
        (handler/generic-handler response)
        :route-name route-name]))
   (table/table-routes)))

(defn generate-routes-by-file
  "generate route from file"
  [path]
  (generate-routes (open-config path)))

(comment
  (generate-routes-by-file "/home/rod/.config/moclojer.yml"))
