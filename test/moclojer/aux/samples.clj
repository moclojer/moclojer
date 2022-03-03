(ns moclojer.aux.samples
  (:require [yaml.core :as yaml]))

(def yaml-sample
  (yaml/parse-string "
- endpoint:
    # Note: the method could be omitted because GET is the default
    method: GET
    path: /hello-world
    response:
      # Note: the status could be omitted because 200 is the default
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          \"hello\": \"Hello, World!\"
        }"))

(def edn-sample
  [{:endpoint {:method :get
               :path "/users"
               :response {:status 200
                          :headers {:content-type  "applicantion/json"}
                          :body {:id 1 :name "chico"}}
               :router-name :get-pet-by-id}}])
