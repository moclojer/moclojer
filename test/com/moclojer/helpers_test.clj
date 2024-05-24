(ns com.moclojer.helpers-test
  (:require
   [com.moclojer.router :as router]
   [com.moclojer.server :refer [reitit-router]]
   [io.pedestal.http :as http]
   [reitit.pedestal :as pedestal]))

(defn service-fn
  "create a service function of pedestal from a config map"
  [config & {:keys [mocks port]
             :or {port 8000}}]
  (-> {::http/routes []
       ::http/type              :jetty
       ::http/port port}

      (http/default-interceptors)
      (pedestal/replace-last-interceptor
       (reitit-router (atom (router/smart-router (merge {::router/config config}
                                                        {::router/mocks mocks})))))

      (http/dev-interceptors)
      (http/create-server)
      ::http/service-fn))
