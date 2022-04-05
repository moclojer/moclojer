(ns moclojer.edn-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.router :as router]))


(deftest dynamic-endpoint-edn
  (let [service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config "test/moclojer/resources/moclojer.edn"})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
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
