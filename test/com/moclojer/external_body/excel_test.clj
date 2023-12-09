(ns com.moclojer.external-body.excel-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [com.moclojer.helpers-test :as helpers]
            [io.pedestal.test :refer [response-for]]
            [yaml.core :as yaml]))

(deftest xlsx-config-test
  (is (= [{:name "avelino", :langs "golang"}
          {:name "chicao", :langs "python"}]
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/xlsx.yml"))
             (response-for :get "/xlsx")
             :body
             (json/parse-string true)))))
