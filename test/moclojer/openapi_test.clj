(ns moclojer.core-test
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.core :as moclojer]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]))

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
                              "headers" {"Content-Type" "application/json"}}})
        service-fn (-> {::http/routes (moclojer/make-router {::openapi/config config})}
                       http/default-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (testing
     "Simple route"
      (is (= [{:id   0
               :name "caramelo"}]
             (-> service-fn
                 (response-for :get "/pets")
                 :body
                 (json/parse-string true)))))
    (testing
     "Not implemented route"
      (is (= 501
             (-> service-fn
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
                                    "headers" {"Content-Type" "application/json"}}})


        service-fn (-> {::http/routes (moclojer/make-router {::openapi/config config})}
                       http/default-interceptors
                       http/create-servlet
                       ::http/service-fn)]
    (testing
     "findPets route"
      (is (= [{:id   0
               :name "caramelo"}]
             (-> service-fn
                 (response-for :get "/pets")
                 :body
                 (json/parse-string true)))))
    (testing
     "addPet route"
      (is (= 303
             (-> service-fn
                 (response-for :post "/pets")
                 :status))))
    (testing
     "find pet by id route"
      (is (= {:id   0
              :name "caramelo"}
             (-> service-fn
                 (response-for :get "/pets/0")
                 :body
                 (json/parse-string true)))))
    (testing
     "deletePet route"
      (is (= 202
             (-> service-fn
                 (response-for :delete "/pets/0")
                 :status))))))
