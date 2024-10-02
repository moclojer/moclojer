(ns com.moclojer.webhook
  (:require
   [clj-http.client :as client]
   [clojure.core.async :as a]
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [com.moclojer.log :as log]))

(defn read-body
  "Returns a json string of either `body`'s parsed content or the error
  that happened during parsing."
  [body]
  (json/write-str
   (try
     (edn/read-string body)
     (catch Exception e
       (log/log :error :webhook-warning
                :invalid-body body
                :message (.getMessage e))
       {:error "failed to read and parse request body"
        :message (.getMessage e)}))))

(defn request-after-delay
  "Performs given `request` after its defined delay time. Returns the
  pre-defined body after execution, or an empty object if `nil`.

  Be cautious that the returned content is the pre-defined response body,
  not the webhook response. Since there's a delay between definition and
  execution, it's not possible to return it.

  `request` should be a map containing (all optional):
    - `:condition`  If false, the request won't be sent         (default: true)
    - `:sleep-time` Delay in seconds before sending the request (default: 60)
    - `:body`       The request body                            (default: `{}`)

  Example:
    (request-after-delay {:url \"http://example.com\"
                          :method :post
                          :body \"{\\\"key\\\":\\\"value\\\"}\"
                          :sleep-time 1000})
    ;; => `\"{\\\"key\\\":\\\"value\\\"}\"`
          (returned immediately, request sent after 1 second)"
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
                (log/log :error :webhook-failed hashed-req :message (.getMessage e))
                (.printStackTrace e))))))
      (log/log :info :sleep sleep-time :webhook-done hashed-req :condition condition))

    (if condition
      (:body request)
      "{}")))
