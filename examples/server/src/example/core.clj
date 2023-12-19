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

(defn start!
  ([]
   (server/start-server! *router))
  ([config-path]
   (server/start-server-with-file-watcher! {:config-path config-path})))

(comment

  ;starting 
  (start!)

  ;starting with a file
  (start! "resources/moclojer.yml"))



