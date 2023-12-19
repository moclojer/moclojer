(ns example.core
  (:require
   [com.moclojer.adapters :as adapters]
   [com.moclojer.server :as server]))

(def *router
  "create a router from a config map"
  (adapters/generate-routes
   [{:endpoint
     {:method "GET"
      :path "/example"
      :response {:status 200
                 :headers {:Content-Type "application/json"}
                 :body {:id 123}}}}]))

(comment
  ;starting 
  (server/start-server! *router)

  ;starting with a file
  (server/start-server-with-file-watcher! {:config-path "resources/moclojer.yml"}))



