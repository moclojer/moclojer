(ns com.moclojer.framework-test
  (:require
   [clojure.test :refer [deftest is]]
   [com.moclojer.adapters :as adapters]
   [com.moclojer.server :as server]))

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
         (:body
          ((server/reitit-router *router)
           {:request-method :get
            :uri "/example"})))))
