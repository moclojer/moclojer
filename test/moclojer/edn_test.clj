(ns moclojer.edn-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.aux.service :refer [service-fn]]
            [moclojer.router :as router]))


(deftest dynamic-endpoint-edn
  (let [config (router/make-edn-specs "moclojer.edn")
        service-fn (service-fn config)]
    (testing "get all pets"
      (is (= {:pets [{:name "Uber" :type "dog"} {:name "Pinpolho" :type "cat"}]}
             (-> service-fn
                 (response-for :get "/pets")
                 :body
                 (json/parse-string true)))))
    (testing "get pet by id"
      (is (= {:id 1 :name "uber" :type "dog"}
             (-> service-fn
                 (response-for :get "/pet/1")
                 :body
                 (json/parse-string true)))))))
