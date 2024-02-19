(ns com.moclojer.reitit-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.adapters :as adapters]
   [yaml.core :as yaml]))

(deftest convert-yml-to-reitit-spec
  (testing "should convert yml to reitit spec"
    (let [route (adapters/generate-routes
                 (yaml/from-file "test/com/moclojer/resources/moclojer.yml")
                 :swggaer? true)]
      (is
       {}
       route))))
