(ns moclojer.mrouter
  (:require [moclojer.openapi :as openapi]
            [moclojer.handler :as handler]))

(defn edn-router
  [{::keys [config]}]
  (handler/generate-pedestal-route-from-edn config))

(defn router
  [{::keys [config]}]
  (if (= (get config "openapi") "3.0.0")
    (openapi/generate-pedestal-route config)
    (handler/generate-pedestal-route config)))

(defn smart-router [specs]
  (if (= (:type specs) :edn)
    (edn-router {::config specs})
    (router {::config specs})))
