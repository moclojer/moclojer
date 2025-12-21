(ns com.moclojer.router-test
  (:require
   [cheshire.core :as json]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.router :as router]
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

(def edn-sample
  [{:endpoint {:method      :get
               :path        "/users"
               :response    {:status  200
                             :headers {:content-type "applicantion/json"}
                             :body    {:id 1 :name "chico"}}
               :router-name :get-pet-by-id}}])

(def postman-sample
  {:info {:name "Test Collection"
          :schema "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"}
   :item [{:name "Get Users"
           :request {:method "GET"
                     :url {:path ["users"]}}
           :response [{:code 200
                       :body "[{\"id\":1}]"}]}]})

(deftest smart-router-test
  (testing "should make edn routers"
    (let [routers (router/smart-router
                   {::router/config edn-sample})]
      (is (= (count routers) 3))
      ;; Router order changed due to http-kit integration and WebSocket support
      (is (= (ffirst routers) ""))
      (is (= (first (get routers 1)) "/users"))
      (is (= (first (get routers 2)) "/swagger.json")))
    (testing "should make yaml routers"
      (let [yaml-routers (router/smart-router
                          {::router/config yaml-sample})]
        (is (= (count yaml-routers) 3)))))

  (testing "should make postman routers"
    (let [routers (router/smart-router
                   {::router/config postman-sample})]
      (is (= (count routers) 3))
      (is (= (ffirst routers) ""))
      (is (= (first (get routers 1)) "/users"))
      (is (= (first (get routers 2)) "/swagger.json"))))

  (testing "should detect postman collection from file"
    (let [collection (json/parse-string
                      (slurp "test/com/moclojer/resources/postman-collection-example.json")
                      true)
          routers (router/smart-router
                   {::router/config collection})]
      ;; Should have: home endpoint + /pets (GET+POST merged) + /pets/:id (GET+DELETE merged) + swagger
      (is (= (count routers) 4))
      (is (= (ffirst routers) "")))))
