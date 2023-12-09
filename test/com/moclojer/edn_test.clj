(ns com.moclojer.edn-test
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [com.moclojer.helpers-test :as helpers]
            [io.pedestal.test :refer [response-for]]))


(deftest dynamic-endpoint-edn
  (let [service-fn (helpers/service-fn (edn/read-string (str "[" (slurp "test/com/moclojer/resources/moclojer.edn") "]")))]
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
