(ns com.moclojer.helpers-test
  (:require [com.moclojer.router :as router]
            [com.moclojer.server :as server]
            [io.pedestal.http :as http]))

(defn service-fn
  "create a service function of pedestal from a config map"
  [config & {:keys [mocks port]}]
  (-> {::http/routes (router/smart-router (merge {::router/config config}
                                                 {::router/mocks mocks}))
       ::http/port port}
      server/get-interceptors
      http/dev-interceptors
      http/default-interceptors
      http/create-servlet
      ::http/service-fn))
