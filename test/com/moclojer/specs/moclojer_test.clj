(ns com.moclojer.specs.moclojer-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.helpers-test :as helpers]
   [com.moclojer.specs.moclojer :refer [build-body create-url make-body
                                        make-path-parameters render-template
                                        enrich-external-body]]
   [com.moclojer.external-body.core :as ext-body]))

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

(deftest moclojer->reitit-test
  (let [server (helpers/service-fn
                "test/com/moclojer/resources/moclojer-v2.yml"
                {:start? false :join? false})]
    (is (= (json/write-str {:user "avelino is 77 years old and has children"})
           (:body (server {:request-method :get
                           :uri "/users/77"}))))))

(deftest build-body-test
  (testing "handles valid JSON string"
    (let [response {:body "{\"name\": \"test\"}"}
          result (build-body response {})]
      (is (= {"name" "test"} result))))

  (testing "returns original content for non-JSON strings"
    (let [response {:body "not-json-content"}
          render-mock (fn [_ _] {:content "not-json-content"})
          result (with-redefs [render-template render-mock]
                   (build-body response {}))]
      (is (= "not-json-content" result))))

  (testing "handles error cases properly"
    (let [response {:body "error-trigger"}
          render-mock (fn [_ _] {:error? true
                                 :content "{\"message\": \"Error occurred\"}"})
          result (with-redefs [render-template render-mock]
                   (build-body response {}))]
      (is (= "Error occurred" (:message result)))
      (is (map? (:error result)))))

  (testing "handles already parsed content"
    (let [response {:body {:already "parsed"}}
          render-mock (fn [_ _] {:content {:already "parsed"}})
          result (with-redefs [render-template render-mock]
                   (build-body response {}))]
      (is (= {:already "parsed"} result))))

  (testing "handles exceptions during JSON parsing"
    (let [response {:body "invalid-json"}
          render-mock (fn [_ _] {:content "invalid-json"})
          json-mock (fn [_] (throw (Exception. "JSON parse error")))
          result (with-redefs [render-template render-mock
                               json/read-str json-mock]
                   (build-body response {}))]
      (is (= "invalid-json" result))))

  (testing "handles external body"
    (let [response {:external-body {:path "/some/path" :content "external content"}}
          enrich-mock (fn [body _] body)
          type-mock (fn [body] (assoc body :content "processed content"))
          render-mock (fn [_ _] {:content "processed content"})
          result (with-redefs [render-template render-mock
                               enrich-external-body enrich-mock
                               ext-body/type-identification type-mock]
                   (build-body response {}))]
      (is (= "processed content" result)))))
