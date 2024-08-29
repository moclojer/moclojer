(ns com.moclojer.specs.moclojer-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.helpers-test :as helpers]
   [com.moclojer.specs.moclojer :refer [create-url make-body
                                        make-path-parameters]]))
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

(deftest make-path-parameters-test
  (testing "make the parameters for path"
    [(is (= {:id :string} (make-path-parameters "/pets/:id|string" true)))
     (is (= {:id :string
             :testing :string} (make-path-parameters "/pets/:id/:testing" true)))
     (is (= {:id :string} (make-path-parameters "/pets/dog/:id" true)))
     (is (= {:number :int
             :id :string} (make-path-parameters "/pets/:number|int/:id|string" true)))
     (is (= {:number :int
             :id :string} (make-path-parameters "/pets/:number|int/:id|string/test" true)))]))

(deftest make-body-test
  (is (= [:map [:username :string]]
         (make-body "{\"username\": \"avelino\"}"
                    :request))))

(deftest ->moclojer->pedestal-test
  (let [server (helpers/service-fn
                "test/com/moclojer/resources/moclojer-v2.yml"
                {:start? false :join? false})]
    (is (= {:user "avelino is 77 years old and has children"}
           (:body (server {:request-method :get
                           :uri "/users/77"}))))))
