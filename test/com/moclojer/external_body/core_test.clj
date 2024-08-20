(ns com.moclojer.external-body.core-test
  (:require
   [clojure.data.json :as jsond]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.external-body.core :as core]
   [com.moclojer.helpers-test :as helpers]
   [io.pedestal.test :refer [response-for]]
   [yaml.core :as yaml]))

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
  (is (= ret-text
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/external-body-json.yml")
                                 {:start? false :join? false})
             (response-for :get "/external-body-text")
             :body
             (jsond/read-str :key-fn keyword)))))

(deftest url-external-config-test
  ;; loop to simplify implementation - no N assert `(is(=))`
  (for [name ["kabuto" "marowak"]]
    (is (= 200
           (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/external-body-json.yml")
                                   {:start? true
                                    :join? true})
               (response-for :get (str "/pokemon/" name))
               :status)))))
