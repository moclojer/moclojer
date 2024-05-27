(ns com.moclojer.webhook-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [com.moclojer.helpers-test :as helpers]
            [com.moclojer.webhook :as webhook]
            [io.pedestal.test :refer [response-for]]
            [yaml.core :as yaml]))

(def body {:id 123})

(deftest server-with-webhook
  (is (= body
         (-> (helpers/service-fn "test/com/moclojer/resources/webhook.yml" {:start? false})
             (response-for :post "/with-webhook")
             :body
             (json/parse-string true)))))

(deftest call-request-after-delay
  (do
    ;; upload the server to test on the internal endpoint
    ;; without the need for an external request
    (helpers/service-fn "test/com/moclojer/resources/webhook.yml" {:start? false})
    (is (= body
           (-> (webhook/request-after-delay
                {:url "http://127.0.0.1:8080/with-webhook"
                 :method :post
                 :body (json/generate-string body)})
               (json/parse-string true))))))

(deftest call-request-after-delay-if
  (do
    (helpers/service-fn "test/com/moclojer/resources/webhook.yml" {:start? false})
    (is (= {}
           (-> (webhook/request-after-delay
                {:url "http://127.0.0.1:8080/with-webhook-if/empty-body"
                 :condition false
                 :method :post
                 :body (json/generate-string body)})
               (json/parse-string true))))))
