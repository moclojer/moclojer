(ns moclojer.external-body.core-test
  (:require [clojure.data.json :as jsond]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.external-body.core :as core]
            [moclojer.router :as router]
            [yaml.core :as yaml]))

(def data-text
  {:provider "json"
   :path "test/moclojer/resources/text-plan.json"})
(def ret-text {:a 123 :b "abc"})

(def data-xlsx
  {:provider "xlsx"
   :sheet-name "test"
   :path "test/moclojer/resources/excel-sample.xlsx"})
(def ret-xlsx [{:name "avelino", :langs "golang"}
               {:name "chicao", :langs "python"}])

(deftest type-identification
  (testing "json file type"
    (is (= ret-text
           (jsond/read-str
            (core/type-identification data-text)
            :key-fn keyword))))

  (testing "xlsx file core/type-identification"
    (is (= ret-xlsx
           (jsond/read-str
            (core/type-identification data-xlsx)
            :key-fn keyword)))))

(deftest text-config-test
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/external-body-json.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= ret-text
           (-> service-fn
               (response-for :get "/external-body-text")
               :body
               (jsond/read-str :key-fn keyword))))))
