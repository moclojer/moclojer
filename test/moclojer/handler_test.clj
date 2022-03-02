(ns moclojer.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [moclojer.handler :as moclojer]
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

(deftest generate-pedestal-route
  (testing "there must be two no items in the (set ...), the one registered
            in yaml and the home (/)"
    (let [routers (moclojer/generate-pedestal-route yaml-sample)]
      (is (= (count routers) 2))
      (is (:path (first routers)) "/")
      (is (:path (last routers)) "/hello-world"))))
