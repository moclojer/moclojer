(ns example.core
  (:require
   [com.moclojer.adapters :as adapters]
   [com.moclojer.server :as server]))

(def routes
  [{:endpoint
    {:method "GET"
     :path "/example"
     :response {:status 200
                :body {:message "ok"}}}}
   {:endpoint
    {:method "GET"
     :path "/example/rate-limit"
     :response {:status 200
                :body {:message "ok"
                       :rate_limited true}}
     :rate-limit {:window-ms 5000    ;; 5 second window
                  :max-requests 1     ;; 3 requests per 5 seconds
                  :key-fn :remote-addr}}}])

(def *router (atom @(adapters/generate-routes routes)))

(defn -main [& args]
  (server/start-server! *router))

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
