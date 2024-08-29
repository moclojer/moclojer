(ns com.moclojer.helpers-test
  (:require
   [com.moclojer.adapters :as adapters]
   [com.moclojer.io-utils :refer [open-file]]
   [com.moclojer.server :as server]))

(defn service-fn
  "create a service function of reitit from a config map"
  [config & {:keys [mocks]}]
  (let [*router (adapters/generate-routes (open-file config)
                                          :mocks-path mocks)]
    (server/reitit-router *router)))

(defn start-server!
  "create a running server of reitit from a config map"
  [config & {:keys [mocks] :as opts}]
  (let [*router (adapters/generate-routes (open-file config)
                                          :mocks-path mocks)]
    (server/start-server!
     (server/reitit-router *router)
     opts)))

(comment
  (.stop
   (start-server!
    "test/com/moclojer/resources/external-body-json.yml"))

  (require '[clojure.pprint])
  (clojure.pprint/pprint
   @(adapters/generate-routes
     (open-file "test/com/moclojer/resources/external-body-json.yml")))

  (clojure.pprint/pprint
   (last
    @(adapters/generate-routes
      (open-file "test/com/moclojer/resources/moclojer-v2.yml"))))

  (def server
    (service-fn "test/com/moclojer/resources/moclojer-v2.yml"))

  (time
   (:body
    (server {:request-method :post
             :uri "/users"
             :body-params {:age 5}})))

  ;
  )


