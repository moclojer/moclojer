(ns moclojer.helpers-test
  (:require [io.pedestal.http :as http]
            [moclojer.router :as router]
            [moclojer.server :as server]))

(defn service-fn
  "create a service function of pedestal from a config map"
  [config & {:keys [mocks]}]
  (-> {::http/routes (router/smart-router (merge {::router/config config}
                                                 {::router/mocks mocks}))}
      server/get-interceptors
      http/dev-interceptors
      http/default-interceptors
      http/create-servlet
      ::http/service-fn))
