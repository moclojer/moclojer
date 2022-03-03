(ns moclojer.core-test
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]
            [moclojer.aux.service :refer [service-fn]]))

(def petstore-spec
  (yaml/parse-string (slurp (io/resource "v3.0/petstore.yaml"))
                     :keywords false))

(def petstore-expanded-spec
  (yaml/parse-string (slurp (io/resource "v3.0/petstore-expanded.yaml"))
                     :keywords false))

(deftest hello-petstore-spec
  (let [config (openapi/with-mocks
                 petstore-spec
                 {"listPets" {"status"  200
                              "body"    (json/generate-string [{:id   0
                                                                :name "caramelo"}])
                              "headers" {"Content-Type" "application/json"}}})]
    (testing
     "Simple route"
      (is (= [{:id   0
               :name "caramelo"}]
             (-> (service-fn config)
                 (response-for :get "/pets")
                 :body
                 (json/parse-string true)))))
    (testing
     "Not implemented route"
      (is (= 501
             (-> (service-fn config)
                 (response-for :post "/pets")
                 :status))))))

(deftest hello-petstore-expanded-spec
  (let [config (openapi/with-mocks
                 petstore-expanded-spec
                 {"find pet by id" {"status"  200
                                    "body"    (json/generate-string {:id   0
                                                                     :name "caramelo"})
                                    "headers" {"Content-Type" "application/json"}}
                  "addPet"         {"status" 303}
                  "deletePet"      {"status" 202}
                  "findPets"       {"status"  200
                                    "body"    (json/generate-string [{:id   0
                                                                      :name "caramelo"}])
                                    "headers" {"Content-Type" "application/json"}}})]
    (testing
     "findPets route"
      (is (= [{:id   0
               :name "caramelo"}]
             (-> (service-fn config)
                 (response-for :get "/pets")
                 :body
                 (json/parse-string true)))))
    (testing
     "addPet route"
      (is (= 303
             (-> (service-fn config)
                 (response-for :post "/pets")
                 :status))))
    (testing
     "find pet by id route"
      (is (= {:id   0
              :name "caramelo"}
             (-> (service-fn config)
                 (response-for :get "/pets/0")
                 :body
                 (json/parse-string true)))))
    (testing
     "deletePet route"
      (is (= 202
             (-> (service-fn config)
                 (response-for :delete "/pets/0")
                 :status))))))
