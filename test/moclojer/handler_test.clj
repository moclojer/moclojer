(ns moclojer.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [moclojer.handler :as moclojer]
            [moclojer.aux.samples :as aux.samples]))

(deftest generate-pedestal-route
  (testing "there must be two no items in the (set ...), the one registered
            in yaml and the home (/)"
    (let [routers (moclojer/generate-pedestal-route aux.samples/yaml-sample)]
      (is (= (count routers) 2))
      (is (= (:path (first routers)) "/"))
      (is (= (:path (last routers)) "/hello-world")))))


(deftest generate-pedestal-edn-route
  (testing  "Should have get users when generate route by edn"
    (let [routers (moclojer/generate-pedestal-edn-route aux.samples/edn-sample)]
      (is (= (count routers) 1))
      (is (= (:path (first routers)) "/users")))))
