(ns moclojer.specs.openapi-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [yaml.core :as yaml]
   [moclojer.specs.openapi :as openapi]))

(def petstore
  {:config "META-INF/openapi-spec/v3.0/petstore-expanded.yaml"
   :mocks "test/moclojer/resources/petstore-expanded-mocks.yaml"})

(defn read-yaml [path]
  (yaml/parse-string (slurp path)))

(deftest openapi->moclojer
  (let [config (read-yaml (:config petstore))
        mocks (read-yaml (:mocks petstore))
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
