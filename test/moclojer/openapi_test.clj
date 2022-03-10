(ns moclojer.openapi-test
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.test :refer [response-for]]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]
            [moclojer.aux.service :refer [service-fn]]
            [io.pedestal.http :as http]
            [moclojer.router :as router])
  (:import (java.io File)))

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
              (response-for :get "/pets"
                :body
                (json/parse-string true))))))
    (testing
     "Not implemented route"
      (is (= 501
            (-> (service-fn config)
              (response-for :post "/pets"
                :status)))))))


(deftest multipart-form
  (run! #(.delete ^File %)
    (reverse (file-seq (io/file "upload-filesystem"))))
  (let [spec (update-in petstore-expanded-spec
               ["paths" "/pets" "post" "requestBody" "content"]
               (fn [content]
                 (assoc content
                   "multipart/form-data"
                   (val (first content)))))
        config (openapi/with-mocks
                 spec
                 {"addPet" {"store"  "upload-filesystem"
                            "status" 303}})
        service-fn (-> {::http/routes (router/smart-router config)}
                     http/default-interceptors
                     http/dev-interceptors
                     http/create-servlet
                     ::http/service-fn)]
    (is (= 303
          (:status (response-for service-fn :post "/pets"
                     :headers {"Content-Type" "multipart/form-data; boundary=XXXX"}
                     :body (str "--XXXX\r\n"
                             "Content-Disposition: form-data; name=\"name\"; filename=\"dog.txt\"\r\n"
                             "Content-Type: text/plain\r\n\r\n"
                             "newdog\r\n"
                             "--XXXX\r\n"
                             "Content-Disposition: form-data; name=\"tag\"\r\n\r\n"
                             "beagle\r\n"
                             "--XXXX--")))))
    (is (= ["tag"
            "dog.txt"]
          (map #(.getName ^File %)
            (.listFiles (first (.listFiles (io/file "upload-filesystem")))))))))


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
