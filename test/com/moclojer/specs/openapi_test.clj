(ns com.moclojer.specs.openapi-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.helpers-test :as helpers]
   [com.moclojer.specs.openapi :as openapi]
   [yaml.core :as yaml]))

(def petstore
  {:config "META-INF/openapi-spec/v3.0/petstore-expanded.yaml"
   :mocks "test/com/moclojer/resources/petstore-expanded-mocks.yaml"})

(deftest openapi->moclojer
  (let [config (yaml/from-file (:config petstore))
        mocks (yaml/from-file (:mocks petstore))
        endpoints (openapi/->moclojer config mocks)]
    (testing "Should convert openapi spec to moclojer spec"
      (is (=
           [{:endpoint {:method "GET"
                        :path "/pets"
                        :response {:status 200
                                   :body "[{\"id\":0,\"name\":\"caramelo\"}]"
                                   :headers {:Content-Type "application/json"}}}},
            {:endpoint {:method "POST"
                        :path "/pets"
                        :response {:status 303}}},
            {:endpoint {:method "GET"
                        :path "/pets/:id"
                        :response {:status 200
                                   :body "{\"id\":0,\"name\":\"caramelo\"}"
                                   :headers {:Content-Type "application/json"}}}},
            {:endpoint {:method "DELETE"
                        :path "/pets/:id"
                        :response {:status 202}}}]
           endpoints)))))

(deftest openapi->moclojer->pedestal
  (let [server (helpers/service-fn (:config petstore)
                                   {:mocks (:mocks petstore)})]
    (is (= {:id 0, :name "caramelo"}
           (:body (server {:request-method :get
                           :uri "/pets/1"}))))))
