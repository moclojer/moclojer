(ns moclojer.external-body.xlsx-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.external-body.xlsx :as xlsx]
            [moclojer.helpers-test :as helpers]
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
  (is (= expected-result
         (-> (helpers/service-fn (yaml/from-file "test/moclojer/resources/xlsx.yml"))
             (response-for :get "/xlsx")
             :body
             (json/parse-string true)))))
