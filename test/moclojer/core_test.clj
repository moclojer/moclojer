(ns moclojer.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [moclojer.core :as moclojer]
            [yaml.core :as yaml]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [cheshire.core :as json]))

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

(deftest make-router
  (testing "there must be two no items in the (set ...), the one registered
            in yaml and the home (/)"
    (let [routers (moclojer/make-router {::moclojer/config yaml-sample})]
      (is (= (count routers) 2))
      (is (:path (first routers)) "/")
      (is (:path (last routers)) "/hello-world"))))

(deftest hello-world
  (let [config (yaml/parse-string (slurp "moclojer.yml"))
        service-fn (-> {::http/routes (moclojer/make-router {::moclojer/config config})}
                       http/default-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:hello "Hello, World!"}
           (-> service-fn
               (response-for :get "/hello-world")
               :body
               (json/parse-string true))))))
