(ns moclojer.core-test
  (:require [clojure.test :refer [deftest is testing]]
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

(deftest make-router
  (testing "there must be two no items in the (set ...), the one registered
            in yaml and the home (/)"
    (let [routers (moclojer/make-router yaml-sample)]
      (is (= (count routers) 2))
      (is (first (first routers)) "/")
      (is (first (last routers)) "/hello-world"))))
