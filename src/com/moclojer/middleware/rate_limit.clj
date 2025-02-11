(ns com.moclojer.middleware.rate-limit
  (:require [com.moclojer.log :as log]))

(def ^:private requests-store (atom {}))

(defn- cleanup-old-requests [requests window-ms now]
  (let [cutoff (- now window-ms)]
    (filterv #(>= % cutoff) requests)))

(defn wrap-rate-limit
  "Rate limiting middleware that limits requests by IP or custom key.
   Options can be configured via endpoint metadata:
   {:rate-limit {:window-ms 900000    ;; 15 minutes
                 :max-requests 100     ;; requests per window
                 :key-fn :remote-addr  ;; function to extract key from request}}"
  [handler & {:keys [now-fn] :or {now-fn #(System/currentTimeMillis)}}]
  (fn [request]
    (let [{:keys [window-ms max-requests key-fn]
           :or {window-ms (* 15 60 1000)
                max-requests 100
                key-fn :remote-addr}} (get-in request [:reitit.core/match :data :rate-limit])
          now (now-fn)
          req-key (key-fn request)
          current-requests (get @requests-store req-key [])
          valid-requests (cleanup-old-requests current-requests window-ms now)]

      (if (>= (count valid-requests) max-requests)
        (do
          (log/log :warn :rate-limit-exceeded :key req-key)
          {:status 429
           :headers {"X-RateLimit-Limit" (str max-requests)
                     "X-RateLimit-Remaining" "0"
                     "X-RateLimit-Reset" (str (+ now window-ms))}
           :body {:error "Rate limit exceeded"}})
        (let [updated-requests (conj (vec (sort valid-requests)) now)]
          (swap! requests-store assoc req-key updated-requests)
          (-> (handler request)
              (update :headers merge
                      {"X-RateLimit-Limit" (str max-requests)
                       "X-RateLimit-Remaining" (str (- max-requests (count updated-requests)))})))))))