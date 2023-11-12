(ns moclojer.webhook-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.router :as router]
            [yaml.core :as yaml]))

(deftest server-with-webhook
  (let [service-fn (-> {::http/routes (router/smart-router
                                       {::router/config (yaml/from-file "test/moclojer/resources/webhook.yml")})}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:id 123}
           (-> service-fn
               (response-for :get "/with-webhook")
               :body
               (json/parse-string true))))))
