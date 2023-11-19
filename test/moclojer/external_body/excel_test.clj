(ns moclojer.external-body.excel-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.helpers-test :as helpers]
            [yaml.core :as yaml]))

(deftest xlsx-config-test
  (is (= [{:name "avelino", :langs "golang"}
          {:name "chicao", :langs "python"}]
         (-> (helpers/service-fn (yaml/from-file "test/moclojer/resources/xlsx.yml"))
             (response-for :get "/xlsx")
             :body
             (json/parse-string true)))))
