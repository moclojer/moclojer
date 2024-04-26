(ns com.moclojer.config-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.config :as config]))

(deftest with-xdg-test
  (testing "It generates an rc name as expected."
    (is (= (str config/xdg-config-home "/moclojer.yml")
           (config/with-xdg "moclojer.yml")))))
