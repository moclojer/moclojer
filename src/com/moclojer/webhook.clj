(ns com.moclojer.webhook
  (:require
   [clj-http.client :as client]
   [clojure.core.async :as a]
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [com.moclojer.log :as log]))

(defn read-body
  [body]
  (json/write-str
   (try
     (edn/read-string body)
     (catch Exception e
       (log/log :error :webhook-warning
                :invalid-body body
                :message (.getMessage e))
       "{}"))))

(defn request-after-delay
  "after a delay call http-request, return body"
  [request]
  (let [req (-> request
                (update :condition #(if (boolean? %) % true))
                (update :headers #(or % {"Content-Type" "application/json"}))
                (update :sleep-time #(or % 60))
                (update :body #(read-body (or % "{}"))))
        {:keys [body condition sleep-time]} req
        hashed-req (-> (assoc req :body-hash (hash body))
                       (dissoc :body))]
    (log/log :info
             :sleep (:sleep-time req)
             :webhook-start hashed-req)

    (if condition
      (a/go
        (a/thread
          (do
            (Thread/sleep (long sleep-time))
            (try
              (client/request req)
              (log/log :info :sleep sleep-time :webhook-done hashed-req)
              (catch Exception e
                (log/log :error :webhook-failed hashed-req)
                (.printStackTrace e))))))
      (log/log :info :sleep sleep-time :webhook-done hashed-req :condition condition))

    (if condition
      (:body request)
      "{}")))
