(ns com.moclojer.middleware.latency-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.moclojer.middleware.latency :as latency]))

(deftest basic-latency-test
  (testing "Basic latency simulation"
    (let [handler (fn [_] {:status 200})
          wrapped (latency/wrap-latency handler)
          request {:reitit.core/match
                   {:data {:latency {:min-ms 50
                                     :max-ms 150}}}}  ;; Aumentado para 150ms
          start-time (System/currentTimeMillis)
          _ (wrapped request)
          elapsed-time (- (System/currentTimeMillis) start-time)]
      (is (<= 50 elapsed-time 160)  ;; Margem de 10ms para overhead
          (format "Tempo decorrido (%d ms) deveria estar entre 50ms e 160ms" elapsed-time)))))

(deftest failure-latency-test
  (testing "Failure simulation"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          request {:reitit.core/match
                   {:data {:latency {:failure-rate 1.0}}}}] ;; Always fail
      (try
        (wrapped-handler request)
        (is false "Should have thrown an exception")
        (catch clojure.lang.ExceptionInfo e
          (is (= 500 (:status (ex-data e))))
          (is (= "Internal Server Error (simulated)"
                 (get-in (ex-data e) [:body :error]))))))))

(deftest timeout-latency-test
  (testing "Timeout simulation"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          request {:reitit.core/match
                   {:data {:latency {:timeout-rate 1.0  ;; Always timeout
                                     :timeout-ms 100}}}}
          start-time (System/currentTimeMillis)]
      (try
        (wrapped-handler request)
        (is false "Should have thrown an exception")
        (catch clojure.lang.ExceptionInfo e
          (let [elapsed-time (- (System/currentTimeMillis) start-time)]
            (is (= 504 (:status (ex-data e))))
            (is (= "Gateway Timeout (simulated)"
                   (get-in (ex-data e) [:body :error])))
            (is (<= 100 elapsed-time))))))))

(deftest no-latency-test
  (testing "No latency when not configured"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          request {:reitit.core/match {:data {}}}
          start-time (System/currentTimeMillis)
          response (wrapped-handler request)
          elapsed-time (- (System/currentTimeMillis) start-time)]
      (is (= 200 (:status response)))
      (is (< elapsed-time 50) "Should not add significant latency"))))