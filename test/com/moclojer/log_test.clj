(ns com.moclojer.log-test
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.log :as log]))

(deftest json-format-logging-test
  (testing "stdout content is formatted as json"
    (log/setup :info :json)
    (is (= {:level "info"
            :msg "testing"
            :hello "moclojer"}
           (select-keys
            (-> (log/log :info :testing :hello :moclojer)
                with-out-str
                (json/read-str :key-fn keyword))
            [:msg :hello :level])))))

(deftest default-format-logging-test
  (testing "stdout content is formatted as default (println)"
    (log/setup :info :default)
    (let [out (-> (log/log :info :testing :hello :moclojer)
                  with-out-str
                  (str/split #" "))
          level (get out 2)
          msg (str/join " " (take-last 3 out))]
      [(is (= level "INFO"))
       (is (= msg ":testing :hello :moclojer\n"))])))
