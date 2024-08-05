(ns com.moclojer.router-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.moclojer.aux.samples :as aux.samples]
            [com.moclojer.router :as router]))

(deftest smart-router-test
  (testing "should make edn routers"
    (let [routers (router/smart-router
                   {::router/config aux.samples/edn-sample})]
      (is (= (count routers) 3))
      (is (= (ffirst routers) "/swagger.json"))
      (is (= (first (get routers 2)) "/users"))
      (is (= (first (get routers 1)) "")))
    (testing "should make yaml routers"
      (let [yaml-routers (router/smart-router
                          {::router/config aux.samples/yaml-sample})]
        (is (= (count yaml-routers) 3))))))
