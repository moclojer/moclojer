(ns com.moclojer.webhook
  (:require [clj-http.client :as client]
            [clojure.core.async :as a]
            [com.moclojer.log :as log]))

(defn request-after-delay
  "after a delay call http-request, return body"
  [{:keys [url method body headers sleep-time condition]
    :or {headers {}
         condition true
         ; in milliseconds, 1 minute is 60000 milliseconds
         sleep-time 60}}]
  (let [req {:url url
             :method method
             :headers headers
             :body body}]
    (log/log :info :sleep sleep-time :webhook-start req)
    (if condition
      (a/go
        (a/thread
          (do
            (Thread/sleep (long sleep-time))
            (client/request req)
            (log/log :info :sleep sleep-time :webhook-done req))))
      (log/log :info :sleep sleep-time :webhook-done req :condition condition))
    body))
