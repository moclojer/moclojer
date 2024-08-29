(ns com.moclojer.helpers-test
  (:require
   [com.moclojer.adapters :as adapters]
   [com.moclojer.io-utils :refer [open-file]]
   [com.moclojer.server :refer [start-server!]]
   [io.pedestal.http :as p.http]
   [io.pedestal.test :refer [response-for]]
   [reitit.http :as http]
   [reitit.http.interceptors.exception :as exception]
   [reitit.pedestal :as pedestal]))

(defn service-fn
  "create a service function of pedestal from a config map"
  [config & {:keys [mocks] :as opts}]
  (let [*router (adapters/generate-routes (open-file config)
                                          :mocks-path mocks)]
    (start-server! *router opts)))

(comment
  (require '[clojure.pprint])
  (clojure.pprint/pprint
   (get
    @(adapters/generate-routes
      (open-file "test/com/moclojer/resources/moclojer.yml"))
    2))

  (clojure.pprint/pprint
   (last
    @(adapters/generate-routes
      (open-file "test/com/moclojer/resources/moclojer-v2.yml"))))

  (def server (-> (service-fn "test/com/moclojer/resources/moclojer-v2.yml"
                              {:start? true :join? false :port 8000})))

  (p.http/stop server)

  (let [router (pedestal/routing-interceptor
                (http/router
                 [""
                  {:interceptors [{:name :nop} (exception/exception-interceptor)]}
                  ["/ok" (fn [_] {:status 200, :body "ok"})]
                  ["/fail" (fn [_] (throw (ex-info "kosh" {})))]]))
        service (-> {:io.pedestal.http/request-logger nil
                     :io.pedestal.http/routes []}
                    (io.pedestal.http/default-interceptors)
                    (pedestal/replace-last-interceptor router)
                    (io.pedestal.http/create-servlet)
                    (:io.pedestal.http/service-fn))]
    (:body (io.pedestal.test/response-for service :get "/ok")))

  (-> (service-fn "test/com/moclojer/resources/moclojer-v2.yml"
                  {:start? false :join? true})
      (response-for :get "/users/1"))
  ;
  )


