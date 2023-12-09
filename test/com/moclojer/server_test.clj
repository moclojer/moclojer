(ns com.moclojer.server-test
  (:require [cheshire.core :as json]
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
