(ns moclojer.external-body.xlsx-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.external-body.xlsx :as xlsx]
            [moclojer.router :as router]
            [yaml.core :as yaml]))

(def expected-result
  [{:name "avelino", :langs "golang"}
   {:name "chicao", :langs "python"}])

(deftest header-keywordize
  (testing "convert header to keywords"
    (is (= expected-result
           (xlsx/header-keywordize
            [["name" "langs"]
             ["avelino" "golang"]
             ["chicao" "python"]])))))

(deftest xlsx-config-test
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/xlsx.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= expected-result
           (-> service-fn
               (response-for :get "/xlsx")
               :body
               (json/parse-string true))))))
