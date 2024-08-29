(ns com.moclojer.external-body.core-test
  (:require
   [clojure.data.json :as jsond]
   [clojure.test :refer [are deftest is testing]]
   [com.moclojer.external-body.core :as core]
   [com.moclojer.helpers-test :as helpers]))

(def data-text
  {:provider "json"
   :path "test/com/moclojer/resources/text-plan.json"})

(def ret-text {:a 123 :b "abc"})

(def data-xlsx
  {:provider "xlsx"
   :sheet-name "test"
   :path "test/com/moclojer/resources/excel-sample.xlsx"})

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
  (let [server (helpers/service-fn
                "test/com/moclojer/resources/external-body-json.yml")]
    (is (= ret-text
           (:body (server {:request-method :get
                           :uri "/external-body-text"}))))))

(deftest url-external-config-test
  (let [server (helpers/service-fn
                "test/com/moclojer/resources/external-body-json.yml")]
    (are [name] (= 200
                   (:status (server {:request-method :get
                                     :uri (str "/pokemon/" name)})))
      ["kabuto" "marowak"])))
