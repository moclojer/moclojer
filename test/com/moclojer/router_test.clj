(ns com.moclojer.router-test
  (:require
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

(deftest smart-router-test
  (testing "should make edn routers"
    (let [routers (router/smart-router
                   {::router/config edn-sample})]
      (is (= (count routers) 3))
      (is (= (ffirst routers) "/swagger.json"))
      (is (= (first (get routers 2)) "/users"))
      (is (= (first (get routers 1)) "")))
    (testing "should make yaml routers"
      (let [yaml-routers (router/smart-router
                          {::router/config yaml-sample})]
        (is (= (count yaml-routers) 3))))))
