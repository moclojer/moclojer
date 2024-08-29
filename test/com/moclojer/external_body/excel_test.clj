(ns com.moclojer.external-body.excel-test
  (:require
   [clojure.test :refer [deftest is]]
   [com.moclojer.helpers-test :as helpers]))

(deftest xlsx-config-test
  (let [server (helpers/service-fn "test/com/moclojer/resources/xlsx.yml")]
    (is (= [{:name "avelino", :langs "golang"}
            {:name "chicao", :langs "python"}]
           (:body
            (server {:request-method :get
                     :uri "/xlsx"}))))))
