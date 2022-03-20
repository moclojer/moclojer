(ns moclojer.aux.service
  (:require [io.pedestal.http :as http]
            [moclojer.router :as router]))

(defn service-fn
  [config]
  (-> {::http/routes (router/smart-router config)}
      http/default-interceptors
      http/create-servlet
      ::http/service-fn))
