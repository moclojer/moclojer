(ns moclojer.specs.openapi-test
  (:require
   [cheshire.core :as json]
   [clojure.test :refer [deftest is testing]]
   [io.pedestal.http :as http]
   [io.pedestal.test :refer [response-for]]
   [moclojer.router :as router]
   [moclojer.specs.openapi :as openapi]
   [yaml.core :as yaml]))

(def petstore
  {:config "META-INF/openapi-spec/v3.0/petstore-expanded.yaml"
   :mocks "test/moclojer/resources/petstore-expanded-mocks.yaml"})

(deftest openapi->moclojer
  (let [config (yaml/from-file (:config petstore))
        mocks (yaml/from-file (:mocks petstore))
        endpoints (openapi/->moclojer config mocks)]

    (testing "Should convert openapi spec to moclojer spec"
      (is (=
           [{:endpoint {:method "get"
                        :path "/pets"
                        :response {:status 200
                                   :body "[{\"id\":0,\"name\":\"caramelo\"}]"
                                   :headers {:Content-Type "application/json"}}}},
            {:endpoint {:method "post"
                        :path "/pets"
                        :response {:status 303}}},
            {:endpoint {:method "get"
                        :path "/pets/:id"
                        :response {:status 200
                                   :body "{\"id\":0,\"name\":\"caramelo\"}"
                                   :headers {:Content-Type "application/json"}}}},
            {:endpoint {:method "delete"
                        :path "/pets/:id"
                        :response {:status 202}}}]
           endpoints)))))

(deftest openapi->moclojer->pedestal
  (let [service-fn (-> {::http/routes (router/smart-router
                                       (yaml/from-file (:config petstore))
                                       (yaml/from-file (:mocks petstore)))}
                       http/default-interceptors
                       http/dev-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (is (= {:id 0, :name "caramelo"}
           (-> service-fn
               (response-for :get "/pets/1")
               :body
               (json/parse-string true))))))
