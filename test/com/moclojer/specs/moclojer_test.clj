(ns com.moclojer.specs.moclojer-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.specs.moclojer :refer [->reitit create-url
                                        make-body-parameters make-parameters]]))

(deftest create-url-test
  (testing "make the url from path"
    [(is (= "/pets/:id" (create-url "/pets/:id|string")))
     (is (= "/pets/:id/:testing" (create-url "/pets/:id/:testing")))
     (is (= "/pets/dog/:id" (create-url "/pets/dog/:id")))
     (is (= "/pets/:number/:id" (create-url "/pets/:number|int/:id|string")))
     (is (= "/pets/:number/:id/test" (create-url "/pets/:number|int/:id|string/test")))])

  (testing "filter queries out"
    [(is (= "/pets/:id" (create-url "/pets/:id|int?name|string")))
     (is (= "/pets/:id" (create-url "/pets/:id|int?name|string{&owner=chico}")))]))

(deftest make-parameters-test
  (testing "make the parameters for path"
    [(is (= {:id string?} (make-parameters "/pets/:id|string")))
     (is (= {:id string? :testing string?} (make-parameters "/pets/:id/:testing")))
     (is (= {:id string?} (make-parameters "/pets/dog/:id")))
     (is (= {:number int? :id string?} (make-parameters "/pets/:number|int/:id|string")))
     (is (= {:number int? :id string?} (make-parameters "/pets/:number|int/:id|string/test")))]))

(deftest make-body-parameters-test
  (testing "request body"
    (is (= {:hello :string
            :bye :int
            :hello2 {:adult :boolean
                     :name :string}}
           (make-body-parameters {:hello "hellooo"
                                  :bye 123
                                  :hello2 {:adult false
                                           :name "hello"}}))))
  (testing "response body"
    (is (= {:body {:pet {:id :int
                         :name :string}}}
           (-> (second (->reitit [{:endpoint
                                   {:path "/pets/:id"
                                    :response (json/write-str
                                               {:status 200
                                                :body {:pet {:id 123
                                                             :name "rex"}}})}}]))
               (get-in [1 :responses 200]))))))
