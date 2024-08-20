(ns com.moclojer.specs.openapi-test
  (:require
   [cheshire.core :as json]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.helpers-test :as helpers]
   [com.moclojer.specs.openapi :as openapi]
   [io.pedestal.test :refer [response-for]]
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
  (is (= {:id 0, :name "caramelo"}
         (try
           (-> (helpers/service-fn (:config petstore)
                                   {:mocks (:mocks petstore)
                                    :start? false
                                    :join? false})
               (response-for :get "/pets/1")
               (:body)
               (json/parse-string true))
           (catch Exception e
             (.printStackTrace e)
             (.getMessage e))))))
