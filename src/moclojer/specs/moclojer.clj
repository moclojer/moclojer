(ns moclojer.specs.moclojer
  (:require [clojure.string :as string]
            [clojure.edn :as edn]
            [yaml.core :as yaml]
            [io.pedestal.log :as log]
            [io.pedestal.http.route :as route]
            [moclojer.handler :as handler])
  (:import (java.io FileNotFoundException)))

(defn open-config
  "open file"
  [path]
  (try
    (if (string/ends-with? path ".edn")
      (edn/read-string (str "[" (slurp path) "]"))
      (yaml/from-file path))
    (catch FileNotFoundException e
      (log/info :open-config (str "file not found" e)))))

(defn generate-route
  "generate route from moclojer spec"
  [endpoints]
  (log/info :mode "moclojer")
  (let [handlers []]
    (conj handlers handler/home-endpoint)
    (for [[path ops] (group-by (juxt :host :method :path)
                               (remove nil? (map :endpoint endpoints)))]
      (for [{:keys [host method response]} ops
            :let [{:keys [status headers body store]} response]]
        (conj handlers
              (route/expand-routes
               (handler/struct-handler
                host method path body status headers store)))))))

(defn generate-route-by-file
  "generate route from file"
  [path]
  (generate-route (open-config path)))
