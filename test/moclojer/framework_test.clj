(ns moclojer.framework-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.adapters :as adapters]
            [moclojer.server :as server]))

(def *router
  "create a router from a config map"
  (adapters/generate-routes
   [{:endpoint
     {:method "GET"
      :path "/example"
      :response {:status 200
                 :headers {:Content-Type "application/json"}
                 :body {:id 123}}}}]))

(deftest framework-test
  (is (= {:id 123}
         (-> (server/start-server! *router :start? false)
             (response-for :get "/example")
             :body
             (json/parse-string true)))))
