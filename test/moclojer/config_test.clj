(ns moclojer.config-test
  (:require [clojure.test :refer [deftest testing is]]
            [moclojer.config :as config]))

(deftest with-xdg-test
  (testing "It generates an rc name as expected."
    (is (= (str config/get-xdg-config-home "/moclojer.yml")
           (config/with-xdg "moclojer.yml")))))
