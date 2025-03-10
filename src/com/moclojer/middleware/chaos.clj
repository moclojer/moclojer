(ns com.moclojer.middleware.chaos
  (:require [com.moclojer.log :as log]))

(defn random-chance? [probability]
  (< (rand) probability))

(defn- wrap-latency
  "Adds random delay between min-ms and max-ms"
  [handler {:keys [min-ms max-ms probability]}]
  (fn [request]
    (when (random-chance? probability)
      (let [delay-ms (+ min-ms (rand-int (max 1 (- max-ms min-ms))))]
        (log/log :debug :adding-chaos-latency :ms delay-ms)
        (Thread/sleep delay-ms)))
    (handler request)))

(defn- wrap-failure
  "Simulates failures by returning status 500"
  [handler {:keys [probability]}]
  (fn [request]
    (if (random-chance? probability)
      (do
        (log/log :debug :chaos-failure-simulated)
        {:status 500
         :body {:error "Chaos Testing: Simulated failure"}})
      (handler request))))

(defn- wrap-corruption
  "Corrupts response randomly"
  [handler {:keys [probability]}]
  (fn [request]
    (let [response (handler request)]
      (if (random-chance? probability)
        (do
          (log/log :debug :chaos-corruption-simulated)
          (update response :body #(assoc % :chaos_corruption true)))
        response))))

(defn wrap-chaos
  "Middleware that applies chaos testing based on route configuration"
  [handler]
  (fn [request]
    (let [{:keys [latency failures corruption]
           :or {latency {:enabled false
                         :min-ms 100
                         :max-ms 1000
                         :probability 0.2}
                failures {:enabled false
                          :probability 0.1}
                corruption {:enabled false
                            :probability 0.1}}} (get-in request [:reitit.core/match :data :chaos])]

      ((cond-> handler
         (:enabled latency) (wrap-latency latency)
         (:enabled failures) (wrap-failure failures)
         (:enabled corruption) (wrap-corruption corruption)) request))))