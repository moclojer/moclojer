(ns moclojer.core-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.router :as router]
            [moclojer.core :as core]
            [yaml.core :as yaml]))

(deftest hello-world
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/moclojer.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:hello "Hello, World!"}
           (-> service-fn
               (response-for :get "/hello-world")
               :body
               (json/parse-string true))))))

(deftest hello-world-different-origin
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/moclojer.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
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
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/moclojer.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:hello "moclojer!"}
           (-> service-fn
               (response-for :get "/hello/moclojer")
               :body
               (json/parse-string true))))))

(deftest with-params
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/moclojer.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:path-params "moclojer" :query-params "moclojer"}
           (-> service-fn
               (response-for :get "/with-params/moclojer?param1=moclojer")
               :body
               (json/parse-string true))))))

(deftest first-post-route
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/moclojer.yml")})}
                       core/get-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:project "moclojer"}
           (-> service-fn
               (response-for :post "/first-post-route"
                             :headers {"Content-Type" "application/json"}
                             :body (json/encode {:project "moclojer"}))
               :body
               (json/parse-string true))))))

(deftest multi-host
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/multihost.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]

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
