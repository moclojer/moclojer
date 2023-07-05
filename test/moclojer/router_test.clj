(ns moclojer.router-test
  (:require [clojure.test :refer [deftest is testing]]
            [moclojer.aux.samples :as aux.samples]
            [moclojer.router :as router]))

(deftest smart-router-test
  (testing "should make edn routers"
    (let [routers (router/smart-router
                   {::router/config aux.samples/edn-sample})]
      (is (= (count routers) 2))
      (is (= (:path (first routers)) "/"))
      (is (= (:path (second routers)) "/users")))
  (testing "should make yaml routers"
    (let [yaml-routers (router/smart-router
                        {::router/config aux.samples/yaml-sample})]
      (is (= (count yaml-routers) 2))))))
