(ns com.moclojer.server-test
  (:require
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.helpers-test :as helpers]))

(deftest moclojer-v1-test
  (let [server (helpers/service-fn "test/com/moclojer/resources/moclojer.yml")]
    [(testing "raw conditions"
       (is (= {:hello "Hello, World!"}
              (:body (server {:request-method :get
                              :uri "/hello-world"})))))

     (testing "different origins"
       (let [server (helpers/service-fn
                     "test/com/moclojer/resources/moclojer.yml")]
         [(is (= {:hello "Hello, World!"}
                 (:body
                  (server {:request-method :get
                           :uri "/hello-world"
                           :headers {"Origin" "http://google.com/"}}))))
          (is (= nil
                 (get-in
                  (server {:request-method :get
                           :uri "/hello-world"})
                  [:headers "Access-Control-Allow-Origin"])))]))

     (testing "dynamic endpoint"
       (is (= {:hello "moclojer!"}
              (:body
               (server {:request-method :get
                        :uri "/hello/moclojer"})))))

     (testing "with params"
       (is (= {:path-params "moclojer"
               :query-params "moclojer"}
              (:body
               (server
                {:request-method :get
                 :uri "/with-params/moclojer"
                 :query-params {:param1 "moclojer"}})))))

     (testing "first post route"
       (is (= {:project "moclojer"}
              (:body
               (server
                {:request-method :post
                 :uri "/first-post-route"
                 :headers {"Content-Type" "application/json"}
                 :body-params {:project "moclojer"}})))))

     (testing "uri with multi paths"
       [(is (= {:hello-v1 "world!"
                :sufix false}
               (:body (server {:request-method :get
                               :uri "/v1/hello/test/world"}))))
        (is (= {:hello-v1 "world!"
                :sufix true}
               (:body (server {:request-method :get
                               :uri "/v1/hello/test/world/with-sufix"}))))
        (is (= {:hello-v1 "hello world!"}
               (:body (server {:request-method :get
                               :uri "/v1/hello"}))))
        (is (= {:hello-v1 "hello world!"}
               (:body (server {:request-method :get
                               :uri "/v1/hello"}))))])

     (testing "multi path param"
       (is (= {:username "moclojer-123"
               :age 10}
              (:body
               (server {:request-method :get
                        :uri "/multi-path-param/moclojer-123/more/10"})))))]))

(deftest multihost-test
  (let [server (helpers/service-fn "test/com/moclojer/resources/multihost.yml")]
    [(is (= {:domain "moclojer.com"}
            (:body
             (server
              {:request-method :get
               :uri "/multihost"
               :headers {"host" "moclojer.com"}}))))
     (is (= {:domain "sub.moclojer.com"}
            (:body
             (server
              {:request-method :get
               :uri "/multihost-sub"
               :headers {"host" "sub.moclojer.com"}}))))]))

(deftest invalid-mock-syntax-test
  (let [server (helpers/service-fn
                "test/com/moclojer/resources/mock-syntax-error.yml")]
    [(is (= 500
            (:status (server {:request-method :get
                              :uri "/helloo/moclojer"}))))
     (is (string/includes?
          (:body (server {:request-method :get
                          :uri "/helloo/moclojer"}))
          "malformed tag arguments"))]))

(deftest moclojer-v2-test
  (let [server (helpers/service-fn "test/com/moclojer/resources/moclojer-v2.yml")]
    [(is (= 200 (:status
                 (server {:request-method :get
                          :uri "/users/1"}))))
     (is  (= {:user "avelino is 1 years old and has children"}
             (:body
              (server
               {:request-method :post
                :uri "/users"
                :headers {"Content-Type" "application/json"}
                :body-params {:age 1}}))))]))
