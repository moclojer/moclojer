(ns moclojer.external-body.excel-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.helpers-test :as helpers]
            [yaml.core :as yaml]))

(def out [{:name "avelino", :langs "golang"}
          {:name "chicao", :langs "python"}])

(deftest xlsx-config-test
  (let [server-fn (helpers/service-fn (yaml/from-file "test/moclojer/resources/excel.yml"))]
    (testing "xlsx config"
      (is (= out
             (-> server-fn
                 (response-for :get "/xlsx")
                 :body
                 (json/parse-string true)))))
    (testing "xls config"
      (is (= out
             (-> server-fn
                 (response-for :get "/xls")
                 :body
                 (json/parse-string true)))))))
