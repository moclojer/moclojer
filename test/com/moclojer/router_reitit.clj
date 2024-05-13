(ns com.moclojer.router-reitit
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.specs.moclojer :refer [->reitit]]
   [yaml.core :as yaml]
   [clojure.string :as string]))

(deftest spec->reitit
  (testing "Converts a spec to a reitit router"
    (let [spec (yaml/from-file "test/com/moclojer/resources/moclojer.yml")]
      (is (= {} (->reitit spec))))))
