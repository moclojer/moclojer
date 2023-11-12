(ns moclojer.webhook
  (:require [clj-http.client :as client]
            [clojure.core.async :as a]
            [moclojer.log :as log]))

(defn request-after-delay
  "after a delay call http-request"
  [url method body & {:keys [sleep headers sleep-func]
                      :or {sleep 60000
                           headers {}
                           sleep-func (a/timeout sleep)}}]
  (let [req {:url url
             :method method
             :headers headers
             :body body}]
    (a/go
      (log/log :info :webhook-start req)
      (sleep-func) ; invoke function, default `(timeout sleep)`
      (a/<! (client/request req))
      (log/log :info :webhook-done req))))
