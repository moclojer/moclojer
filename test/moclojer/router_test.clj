(ns moclojer.router-test
  (:require [clojure.test :refer [deftest is testing]]
            [moclojer.aux.samples :as aux.samples]
            [moclojer.io-aux :refer [write-config]]
            [moclojer.router :as router]))

(deftest smart-router-test
  (testing "should make edn routers"
    (let [routers (router/smart-router
                   {::router/config (write-config "edn" aux.samples/edn-sample)})]
      (is (= (count routers) 1))
      (is (= (:path (first routers)) "/users"))))
  (testing "should make yaml routers"
    (let [yaml-routers (router/smart-router
                        {::router/config (write-config "yaml" aux.samples/yaml-sample)})]
      (is (= (count yaml-routers) 2)))))
