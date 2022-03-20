(ns moclojer.openapi-test
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [moclojer.io-utils :as iou]
            [moclojer.router :as router]
            [yaml.core :as yaml])
  (:import (java.io File)))

(def openapi-examples
  (io/file "OpenAPI-Specification" "examples"))

(def petstore-spec
  (yaml/parse-string (slurp (io/file openapi-examples "v3.0" "petstore.yaml"))
                     :keywords false))

(def petstore-expanded-spec
  (yaml/parse-string (slurp (io/file openapi-examples "v3.0" "petstore-expanded.yaml"))
                     :keywords false))

(deftest hello-petstore-spec
  (let [mocks {"listPets" {"status"  200
                           "body"    (json/generate-string [{:id   0
                                                             :name "caramelo"}])
                           "headers" {"Content-Type" "application/json"}}}
        service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config (iou/write-config "yaml" petstore-spec)
                                        ::router/mocks  (iou/write-config "yaml" mocks)})}
                       http/default-interceptors
                       http/dev-interceptors
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


(deftest multipart-form
  (run! #(.delete ^File %)
        (reverse (file-seq (io/file "upload-filesystem"))))
  (let [spec (update-in petstore-expanded-spec
                        ["paths" "/pets" "post" "requestBody" "content"]
                        (fn [content]
                          (assoc content
                                 "multipart/form-data"
                                 (val (first content)))))
        mocks {"addPet" {"store"  "upload-filesystem"
                         "status" 303}}
        service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config (iou/write-config "yaml" spec)
                                        ::router/mocks  (iou/write-config "yaml" mocks)})}
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
    (is (= ["name" "tag"]
           (sort (map #(.getName ^File %)
                      (.listFiles (first (.listFiles (io/file "upload-filesystem"))))))))))


(deftest hello-petstore-expanded-spec
  (let [mocks {"find pet by id" {"status"  200
                                 "body"    (json/generate-string {:id   0
                                                                  :name "caramelo"})
                                 "headers" {"Content-Type" "application/json"}}
               "addPet"         {"status" 303}
               "deletePet"      {"status" 202}
               "findPets"       {"status"  200
                                 "body"    (json/generate-string [{:id   0
                                                                   :name "caramelo"}])
                                 "headers" {"Content-Type" "application/json"}}}
        service-fn (-> {::http/routes (router/make-smart-router
                                       {::router/config (iou/write-config "yaml" petstore-expanded-spec)
                                        ::router/mocks  (iou/write-config "yaml" mocks)})}
                       http/default-interceptors
                       http/dev-interceptors
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
