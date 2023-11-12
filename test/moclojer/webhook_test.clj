(ns moclojer.webhook-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.router :as router]
            [moclojer.webhook :as webhook]
            [yaml.core :as yaml]))

(def body {:id 123})

(deftest server-with-webhook
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/webhook.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= body
           (-> service-fn
               (response-for :post "/with-webhook")
               :body
               (json/parse-string true))))))

(deftest call-request-after-delay
  (do
    ;; upload the server to test on the internal endpoint
    ;; without the need for an external request
    (-> {::http/routes (router/smart-router
                        {::router/config (yaml/from-file "test/moclojer/resources/webhook.yml")})
         ::http/port 8000}
        http/default-interceptors
        http/dev-interceptors
        http/create-servlet
        ::http/service-fn)
    (is (= body
           (-> (webhook/request-after-delay
                {:url "http://127.0.0.1:8000/with-webhook"
                 :method :post
                 :body (json/generate-string body)})
               (json/parse-string true))))))
