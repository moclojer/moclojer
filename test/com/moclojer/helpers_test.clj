(ns com.moclojer.helpers-test
  (:require
   [com.moclojer.adapters :as adapters]
   [com.moclojer.io-utils :refer [open-file]]
   [com.moclojer.server :refer [reitit-router]]))

(defn service-fn
  "create a service function of pedestal from a config map"
  [config & {:keys [mocks]}]
  (let [*router (adapters/generate-routes (open-file config)
                                          :mocks-path mocks)]
    (reitit-router *router)))

(comment
  (require '[clojure.pprint])
  (clojure.pprint/pprint
   (get
    @(adapters/generate-routes
      (open-file "test/com/moclojer/resources/moclojer-v2.yml"))
    2))

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


