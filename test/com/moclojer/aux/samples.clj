(ns com.moclojer.aux.samples
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [ring.util.mime-type :as mime]
            [yaml.core :as yaml]))

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
  [{:endpoint {:method      :get
               :path        "/users"
               :response    {:status  200
                             :headers {:content-type "applicantion/json"}
                             :body    {:id 1 :name "chico"}}
               :router-name :get-pet-by-id}}])


(defonce *http-state (atom nil))

(defn sample-upload-server
  []
  (let [routes #{["/" :get (fn [_]
                             {:headers {"Content-Type" (mime/default-mime-types "html")}
                              :body    (str
                                        "<form method='post' enctype='multipart/form-data'>"
                                        "<input type='file' name='name'>"
                                        "<input name='tag'>"
                                        "<input type='submit'>"
                                        "</form>"
                                        "")
                              :status  200})
                  :route-name ::html-form]
                 ["/" :post [(middlewares/multipart-params)
                             (fn [{:keys [multipart-params]}]
                               (doseq [[k v] multipart-params]
                                 (prn [(keyword k)
                                       (or (some-> v :tempfile slurp)
                                           v)]))
                               {:headers {"Location" "/"}
                                :status  303})]
                  :route-name ::post-handler]}]
    (swap! *http-state
           (fn [st]
             (some-> st http/stop)
             (-> {::http/routes routes
                  ::http/type   :jetty
                  ::http/join?  false
                  ::http/port   8080}
                 http/default-interceptors
                 http/create-server
                 http/start)))))
(comment
  (sample-upload-server))
