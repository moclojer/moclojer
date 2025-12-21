(ns com.moclojer.specs.postman-test
  (:require
   [cheshire.core :as json]
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.specs.postman :as postman]))

(def sample-collection
  "Sample Postman Collection for testing"
  {:info {:name "Test API"
          :schema "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"}
   :item [{:name "Get Users"
           :request {:method "GET"
                     :url {:raw "http://localhost:3000/users"
                           :path ["users"]}
                     :header [{:key "Content-Type"
                               :value "application/json"}]}
           :response [{:code 200
                       :body "[{\"id\":1,\"name\":\"Alice\"}]"
                       :header [{:key "Content-Type"
                                 :value "application/json"}]}]}
          {:name "Get User by ID"
           :request {:method "GET"
                     :url {:path ["users" ":id"]}}
           :response [{:code 200
                       :body "{\"id\":1,\"name\":\"Alice\"}"}]}
          {:name "Folder"
           :item [{:name "Create User"
                   :request {:method "POST"
                             :url "http://localhost:3000/users"}
                   :response [{:code 201
                               :body "{\"id\":2,\"name\":\"Bob\"}"}]}
                  {:name "Delete User"
                   :request {:method "DELETE"
                             :url {:path ["users" ":id"]}}
                   :response [{:code 204}]}]}]})

(deftest postman->moclojer-basic
  (testing "Should convert Postman collection to moclojer spec"
    (let [endpoints (postman/->moclojer sample-collection)]
      (is (= 4 (count endpoints)))

      ;; First endpoint: GET /users
      (is (= {:endpoint {:method "GET"
                         :path "/users"
                         :response {:status 200
                                    :body "[{\"id\":1,\"name\":\"Alice\"}]"
                                    :headers {"Content-Type" "application/json"}}}}
             (first endpoints)))

      ;; Second endpoint: GET /users/:id
      (is (= {:endpoint {:method "GET"
                         :path "/users/:id"
                         :response {:status 200
                                    :body "{\"id\":1,\"name\":\"Alice\"}"}}}
             (second endpoints)))

      ;; Third endpoint: POST /users (from folder)
      (is (= {:endpoint {:method "POST"
                         :path "/users"
                         :response {:status 201
                                    :body "{\"id\":2,\"name\":\"Bob\"}"}}}
             (nth endpoints 2)))

      ;; Fourth endpoint: DELETE /users/:id (from folder)
      (is (= {:endpoint {:method "DELETE"
                         :path "/users/:id"
                         :response {:status 204}}}
             (nth endpoints 3))))))

(deftest postman->moclojer-url-formats
  (testing "Should handle different URL formats"
    (let [collection {:info {:name "URL Tests"}
                      :item [{:request {:method "GET"
                                        :url "http://localhost:3000/api/items"}}
                             {:request {:method "GET"
                                        :url {:path ["api" "items" ":id"]}}}
                             {:request {:method "GET"
                                        :url "http://example.com/api/search?q=test"}}]}
          endpoints (postman/->moclojer collection)]

      (is (= "/api/items" (get-in endpoints [0 :endpoint :path])))
      (is (= "/api/items/:id" (get-in endpoints [1 :endpoint :path])))
      ;; Query params should be stripped
      (is (= "/api/search" (get-in endpoints [2 :endpoint :path]))))))

(deftest postman->moclojer-disabled-headers
  (testing "Should skip disabled headers in response"
    (let [collection {:info {:name "Header Tests"}
                      :item [{:request {:method "GET"
                                        :url "/test"}
                               :response [{:code 200
                                           :header [{:key "Enabled-Header"
                                                     :value "value1"}
                                                    {:key "Disabled-Header"
                                                     :value "value2"
                                                     :disabled true}]}]}]}
          endpoints (postman/->moclojer collection)
          headers (get-in endpoints [0 :endpoint :response :headers])]

      (is (= "value1" (get headers "Enabled-Header")))
      (is (nil? (get headers "Disabled-Header"))))))

(deftest postman->moclojer-no-response-example
  (testing "Should provide default response when no examples"
    (let [collection {:info {:name "No Response"}
                      :item [{:request {:method "GET"
                                        :url "/test"}}]}
          endpoints (postman/->moclojer collection)
          response (get-in endpoints [0 :endpoint :response])]

      (is (= 200 (:status response)))
      (is (= "{}" (:body response)))
      (is (= {"Content-Type" "application/json"} (:headers response))))))

(deftest postman->moclojer-nested-folders
  (testing "Should flatten nested folders"
    (let [collection {:info {:name "Nested"}
                      :item [{:name "Level 1"
                              :item [{:name "Level 2"
                                      :item [{:request {:method "GET"
                                                        :url "/deep"}}]}]}]}
          endpoints (postman/->moclojer collection)]

      (is (= 1 (count endpoints)))
      (is (= "/deep" (get-in endpoints [0 :endpoint :path]))))))

(deftest postman->moclojer-from-file
  (testing "Should convert real Postman collection file"
    (let [collection (json/parse-string
                      (slurp "test/com/moclojer/resources/postman-collection-example.json")
                      true)
          endpoints (postman/->moclojer collection)]

      (is (= 4 (count endpoints)))

      ;; Verify GET /pets
      (is (some #(and (= "GET" (get-in % [:endpoint :method]))
                      (= "/pets" (get-in % [:endpoint :path])))
                endpoints))

      ;; Verify GET /pets/:id
      (is (some #(and (= "GET" (get-in % [:endpoint :method]))
                      (= "/pets/:id" (get-in % [:endpoint :path])))
                endpoints))

      ;; Verify POST /pets (from folder)
      (is (some #(and (= "POST" (get-in % [:endpoint :method]))
                      (= "/pets" (get-in % [:endpoint :path])))
                endpoints))

      ;; Verify DELETE /pets/:id (from folder)
      (is (some #(and (= "DELETE" (get-in % [:endpoint :method]))
                      (= "/pets/:id" (get-in % [:endpoint :path])))
                endpoints)))))
