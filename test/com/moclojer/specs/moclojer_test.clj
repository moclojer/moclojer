(ns com.moclojer.specs.moclojer-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.specs.moclojer :refer [create-url make-body-parameters
                                        make-parameters]]))

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
  (is (= {:hello :string
          :bye :int
          :hello2 {:adult :boolean
                   :name :string}}
         (make-body-parameters {:hello "hellooo"
                                :bye 123
                                :hello2 {:adult false
                                         :name "hello"}}))))
;; TODO
#_(deftest ->reitit-test
    (is (= ["/pets/:id"
            {:host "localhost"
             :swagger {:tags ["get-localhost-pets--id|string"]}
             :parameters {:path {:id :string}}
             :responses {200 {:body {:id :int
                                     :name :string}}}
             :get {:summary "Generated from /pets/:id"}}]
           (second (->reitit [{:endpoint {:path "/pets/:id"
                                          :response {:status 400
                                                     :body {:pet {:id 123
                                                                  :name "rex"}}}}}])))))
