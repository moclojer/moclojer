(ns moclojer.core-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.core :as moclojer]
            [yaml.core :as yaml]))

(def yaml-sample
  (yaml/parse-string "
- endpoint:
    # Note: the method could be omitted because GET is the default
    method: GET
    path: /hello-world
    response:
      # Note: the status could be omitted because 200 is the default
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          \"hello\": \"Hello, World!\"
        }"))

(defn service-fn
  [config]
  (-> {::http/routes (moclojer/make-router {::moclojer/config config})}
      http/default-interceptors
      http/create-servlet
      ::http/service-fn))

(deftest make-router
  (testing "there must be two no items in the (set ...), the one registered
            in yaml and the home (/)"
    (let [routers (moclojer/make-router {::moclojer/config yaml-sample})]
      (is (= (count routers) 2))
      (is (:path (first routers)) "/")
      (is (:path (last routers)) "/hello-world"))))

(deftest hello-world
  (let [config (yaml/parse-string (slurp "moclojer.yml"))
        service-fn (service-fn config)]
    (is (= {:hello "Hello, World!"}
           (-> service-fn
               (response-for :get "/hello-world")
               :body
               (json/parse-string true))))))

(deftest dyanamic-endpoint
  (let [config (yaml/parse-string (slurp "moclojer.yml"))
        service-fn (service-fn config)]
    (is (= {:hello "moclojer!"}
           (-> service-fn
               (response-for :get "/hello/moclojer")
               :body
               (json/parse-string true))))))

(deftest with-params
  (let [config (yaml/parse-string (slurp "moclojer.yml"))
        service-fn (service-fn config)]
    (is (= {:path-params "moclojer" :query-params "moclojer"}
           (-> service-fn
               (response-for :get "/with-params/moclojer?param1=moclojer")
               :body
               (json/parse-string true))))))

(deftest first-post-route
  (let [config (yaml/parse-string (slurp "moclojer.yml"))
        service-fn (service-fn config)]
    (is (= {:project "moclojer"}
           (-> service-fn
               (response-for :post "/first-post-route"
                             :headers {"Content-Type" "application/json"}
                             :body (json/encode {:project "moclojer"}))
               :body
               (json/parse-string true))))))
