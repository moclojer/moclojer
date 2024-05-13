(ns com.moclojer.specs.moclojer-spec
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.specs.moclojer :refer [create-url make-parameters]]))

(deftest create-url-test
  (testing "make the url from path"
    (is (= "/pets/:id" (create-url "/pets/:id|string")))
    (is (= "/pets/:id/:testing" (create-url "/pets/:id/:testing")))
    (is (= "/pets/dog/:id" (create-url "/pets/dog/:id")))
    (is (= "/pets/:number/:id" (create-url "/pets/:number|int/:id|string")))
    (is (= "/pets/:number/:id/test" (create-url "/pets/:number|int/:id|string/test")))))

(deftest make-parameters-teste
  (testing "make the parameters for path"
    (is (= {"id" string?} (make-parameters "/pets/:id|string")))
    (is (= {"id" string? "testing" string?} (make-parameters "/pets/:id/:testing")))
    (is (= {"id" string?} (make-parameters "/pets/dog/:id")))
    (is (= {"number" int? "id" string?} (make-parameters "/pets/:number|int/:id|string")))
    (is (= {"number" int? "id" string?} (make-parameters "/pets/:number|int/:id|string/test")))))

