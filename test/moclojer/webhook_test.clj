(ns moclojer.webhook-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.helpers-test :as helpers]
            [moclojer.webhook :as webhook]
            [yaml.core :as yaml]))

(def body {:id 123})

(deftest server-with-webhook
  (is (= body
         (-> (helpers/service-fn (yaml/from-file "test/moclojer/resources/webhook.yml"))
             (response-for :post "/with-webhook")
             :body
             (json/parse-string true)))))

(deftest call-request-after-delay
  (do
    ;; upload the server to test on the internal endpoint
    ;; without the need for an external request
    (helpers/service-fn (yaml/from-file "test/moclojer/resources/webhook.yml")
                        :port 8000)
    (is (= body
           (-> (webhook/request-after-delay
                {:url "http://127.0.0.1:8000/with-webhook"
                 :method :post
                 :body (json/generate-string body)})
               (json/parse-string true))))))
