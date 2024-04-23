(ns com.moclojer.log-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.moclojer.log :as log]
            [clojure.data.json :as json]))

(deftest json-format-logging-test
  (testing "stdout content is formatted as json"
    (log/setup :info :json)
    (is (= {:level "info"
            :msg "testing"
            :hello "moclojer"})
        (select-keys
          (-> (log/log :info :testing :hello :moclojer)
              with-out-str
              (json/read-str :key-fn keyword))
          ["msg" "hello" "level"]))))
