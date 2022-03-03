(ns moclojer.router-test
  (:require [clojure.test :refer [deftest is testing]]
            [moclojer.router :as router]
            [moclojer.aux.samples :as aux.samples]))

(deftest smart-router-test
  (testing "should make edn routers"
    (let [routers (router/smart-router {:endpoints aux.samples/edn-sample :type :edn})]
      (is (= (count routers) 1))
      (is (= (:path (first routers)) "/users"))))
  (testing "should make yaml routers"
    (let [yaml-routers (router/smart-router aux.samples/yaml-sample)]
      (is (= (count yaml-routers) 2)))))
