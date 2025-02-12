(ns com.moclojer.middleware.rate-limit
  (:require [com.moclojer.log :as log]
            [clojure.data.json :as json]))

(def ^:private requests-store (atom {}))

(defn- cleanup-old-requests [requests window-ms now]
  (let [cutoff (- now window-ms)]
    (filterv #(>= % cutoff) requests)))

(defn- get-client-ip [request]
  (or (get-in request [:headers "x-forwarded-for"])
      (get-in request [:remote-addr])
      "127.0.0.1"))

(defn wrap-rate-limit
  "Rate limiting middleware that limits requests by IP or custom key.
   Options can be configured via endpoint metadata:
   {:rate-limit {:window-ms 900000    ;; 15 minutes
                 :max-requests 100     ;; requests per window
                 :key-fn :remote-addr  ;; function to extract key from request}}"
  ([handler]
   (wrap-rate-limit handler {}))
  ([handler opts]
   (let [default-opts {:window-ms (* 15 60 1000)
                       :max-requests 100
                       :key-fn :remote-addr}]
     (fn [request]
       (let [match (get request :reitit.core/match)
             route-opts (get-in match [:data :data :rate-limit]) ;; TODO: `:data :data` it's very ugly, we need to make it more elegant in the future
             config (merge default-opts route-opts opts) ;; default-opts are overwritten by route-opts and opts
             {:keys [window-ms max-requests key-fn]} config
             now (System/currentTimeMillis)
             req-key (if (fn? key-fn)
                       (key-fn request)
                       (if (keyword? key-fn)
                         (get request key-fn (get-client-ip request))
                         (get-client-ip request)))
             current-requests (get @requests-store req-key [])
             valid-requests (cleanup-old-requests current-requests window-ms now)]
         (if (>= (count valid-requests) max-requests)
           (do
             (log/log :warn :rate-limit-exceeded :key req-key)
             {:status 429
              :headers {"Content-Type" "application/json"
                        "X-RateLimit-Limit" (str max-requests)
                        "X-RateLimit-Remaining" "0"
                        "X-RateLimit-Reset" (str (+ now window-ms))}})
           (let [updated-requests (conj valid-requests now)]
             (swap! requests-store assoc req-key updated-requests)
             (-> (handler request)
                 (update :headers merge
                         {"X-RateLimit-Limit" (str max-requests)
                          "X-RateLimit-Remaining" (str (- max-requests (count updated-requests)))})))))))))