(ns moclojer.aux.service
  (:require [moclojer.mrouter :as mrouter]
            [io.pedestal.http :as http]))

(defn service-fn
  [config]
  (-> {::http/routes (mrouter/smart-router config)}
      http/default-interceptors
      http/create-servlet
      ::http/service-fn))
