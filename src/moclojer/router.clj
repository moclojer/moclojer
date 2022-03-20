(ns moclojer.router
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.pedestal.log :as log]
            [moclojer.handler :as handler]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]))

(defn make-smart-router
  "Returns a pedestal routes"
  [{::keys [config mocks]}]
  (-> (if mocks
        (do
          (log/info :mode "openapi")
          (openapi/with-mocks
            (yaml/from-file config false)
            (yaml/from-file mocks false)))
        (do
          (log/info :mode "moclojer")
          (handler/moclojer->openapi
           (if (string/ends-with? config ".edn")
             (edn/read-string (str "[" (slurp config) "]"))
             (cons handler/home-endpoint
                   (yaml/from-file config))))))
      openapi/generate-pedestal-route))
