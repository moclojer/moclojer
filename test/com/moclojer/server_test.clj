(ns com.moclojer.server-test
  (:require
   [cheshire.core :as json]
   [clojure.string :as string]
   [clojure.test :refer [deftest is]]
   [com.moclojer.helpers-test :as helpers]
   [io.pedestal.test :refer [response-for]]
   [yaml.core :as yaml]))

(deftest hello-world
  (is (= {:hello "Hello, World!"}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/hello-world")
             :body
             (json/parse-string true)))))

(deftest hello-world-different-origin
  (let [service-fn (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))]
    (is (= {:hello "Hello, World!"}
           (-> service-fn
               (response-for :get "/hello-world" :headers {"Origin" "http://google.com/"})
               :body
               (json/parse-string true))))
    (is (= nil
           (get-in
            (-> service-fn
                (response-for :get "/hello-world")
                :body
                (json/parse-string true))
            [:headers "Access-Control-Allow-Origin"])))))

(deftest dyanamic-endpoint
  (is (= {:hello "moclojer!"}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/hello/moclojer")
             :body
             (json/parse-string true)))))

(deftest with-params
  (is (= {:path-params "moclojer" :query-params "moclojer"}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/with-params/moclojer?param1=moclojer")
             :body
             (json/parse-string true)))))

(deftest first-post-route
  (is (= {:project "moclojer"}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :post "/first-post-route"
                           :headers {"Content-Type" "application/json"}
                           :body (json/encode {:project "moclojer"}))
             :body
             (json/parse-string true)))))

(deftest multi-host
  (let [service-fn (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/multihost.yml"))]
    (is (= {:domain "moclojer.com"}
           (-> service-fn
               (response-for :get "moclojer.com/multihost")
               :body
               (json/parse-string true))))
    (is (= {:domain "sub.moclojer.com"}
           (-> service-fn
               (response-for :get "sub.moclojer.com/multihost-sub")
               :body
               (json/parse-string true))))))

(deftest uri-with-multi-paths
  (is (= {:hello-v1 "world!"
          :sufix false}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/v1/hello/test/world")
             :body
             (json/parse-string true))))
  (is (= {:hello-v1 "world!"
          :sufix true}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/v1/hello/test/world/with-sufix")
             :body
             (json/parse-string true))))
  (is (= {:hello-v1 "hello world!"}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/v1/hello")
             :body
             (json/parse-string true))))
  (is (= {:hello-v1 "hello world!"}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/v1/hello/")
             :body
             (json/parse-string true)))))

(deftest multi-path-param
  (is (= {:username "moclojer-123"
          :age 10}
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/moclojer.yml"))
             (response-for :get "/multi-path-param/moclojer-123/more/10")
             :body
             (json/parse-string true)))))

(deftest mock-syntax-error
  (is (= 500
         (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/mock-syntax-error.yml"))
             (response-for :get "/helloo/moclojer")
             :status)))
  (is (string/includes?
       (-> (helpers/service-fn (yaml/from-file "test/com/moclojer/resources/mock-syntax-error.yml"))
           (response-for :get "/helloo/moclojer")
           :body)
       "error")))
