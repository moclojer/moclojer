(ns com.moclojer.middleware.latency
  (:require [com.moclojer.log :as log]))

;; Funções de tempo configuráveis para facilitar testes
(def ^:dynamic *sleep-fn* Thread/sleep)
(def ^:dynamic *current-time-fn* #(System/currentTimeMillis))

(defn wrap-latency
  "Simulates network latency and failures.
   Options can be configured via endpoint metadata:
   {:latency {:min-ms 100          ;; minimum latency in ms
              :max-ms 1000         ;; maximum latency in ms
              :failure-rate 0.01   ;; probability of 500 error
              :timeout-rate 0.005  ;; probability of timeout
              :timeout-ms 30000}}" ;; timeout duration
  [handler]
  (fn [request]
    (let [{:keys [min-ms max-ms failure-rate timeout-rate timeout-ms]
           :or {min-ms 0
                max-ms 0
                failure-rate 0
                timeout-rate 0
                timeout-ms 30000}} (get-in request [:reitit.core/match :data :latency])]

      (when (pos? failure-rate)
        (when (< (rand) failure-rate)
          (log/log :debug :simulated-failure)
          (throw (ex-info "Simulated failure"
                          {:status 500
                           :body {:error "Internal Server Error (simulated)"}}))))

      (when (pos? timeout-rate)
        (when (< (rand) timeout-rate)
          (log/log :debug :simulated-timeout)
          (*sleep-fn* timeout-ms)
          (throw (ex-info "Simulated timeout"
                          {:status 504
                           :body {:error "Gateway Timeout (simulated)"}}))))

      (when (pos? max-ms)
        (let [latency (+ min-ms (rand-int (inc (- max-ms min-ms))))]
          (log/log :debug :adding-latency :ms latency)
          (*sleep-fn* latency)))

      (handler request))))