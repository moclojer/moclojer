(ns moclojer.aux.service
  (:require [moclojer.router :as router]
            [io.pedestal.http :as http]))

(defn service-fn
  [config]
  (-> {::http/routes (router/smart-router config)}
      http/default-interceptors
      http/create-servlet
      ::http/service-fn))
