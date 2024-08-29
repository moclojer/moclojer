(ns com.moclojer.edn-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.helpers-test :as helpers]))

(deftest dynamic-endpoint-edn
  (let [server (helpers/service-fn
                "test/com/moclojer/resources/moclojer.edn")]
    [(testing "get all pets"
       (is (= {:pets [{:name "Uber" :type "dog"}
                      {:name "Pinpolho" :type "cat"}]}
              (json/read-str
               (slurp
                (:body (server {:request-method :get
                                :uri "/pets"})))
               :key-fn keyword))))
     (testing "get pet by id"
       (is (= {:id 1
               :name "uber"
               :type "dog"}
              (json/read-str
               (slurp
                (:body (server {:request-method :get
                                :uri "/pet/1"})))
               :key-fn keyword))))]))
