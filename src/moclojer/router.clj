(ns moclojer.router
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.pedestal.log :as log]
            [moclojer.handler :as handler]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml])
  (:import (java.io FileNotFoundException)))

(defn make-smart-router
  "returns a pedestal routes"
  [{::keys [config mocks]}]
  (-> (if mocks
        (do
          (log/info :mode "openapi")
          (try
            (openapi/with-mocks
              (yaml/from-file config false)
              (yaml/from-file mocks false))
            (catch FileNotFoundException e (log/info :openfile (str "config file not found" e)))))
        (do
          (log/info :mode "moclojer")
          (try
            (handler/moclojer->openapi
             (if (string/ends-with? config ".edn")
               (edn/read-string (str "[" (slurp config) "]"))
               (cons handler/home-endpoint
                     (yaml/from-file config))))
            (catch FileNotFoundException e (log/info :openfile (str "config file not found" e))))))
      openapi/generate-pedestal-route))
