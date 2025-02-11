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
                 :body :ok}}}
    {:endpoint
     {:method "GET"
      :path "/example/rate-limit"
      :response {:status 200
                 :body :ok}
      :rate-limit {:window-ms 60000 ; 1 minute window
                   :max-requests 2  ; Allow 2 requests per window
                   :key-fn :remote-addr}}}]))

;; start the server with `clj -A:dev -m example.core`
(defn -main
  ([]
   (server/start-server! *router))
  ([config-path]
   (server/start-server-with-file-watcher! {:config-path config-path})))

(comment

  ;starting 
  (-main)

  ;starting with a file
  (-main "resources/moclojer.yml"))
