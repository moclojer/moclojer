(ns moclojer.core-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.router :as router]))

(deftest hello-world
  (let [service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config "moclojer.yml"})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:hello "Hello, World!"}
           (-> service-fn
               (response-for :get "/hello-world")
               :body
               (json/parse-string true))))))

(deftest dyanamic-endpoint
  (let [service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config "moclojer.yml"})}
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
  (let [service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config "moclojer.yml"})}
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
  (let [service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config "moclojer.yml"})}
                       http/default-interceptors
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
