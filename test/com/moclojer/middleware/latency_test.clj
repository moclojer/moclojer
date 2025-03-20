(ns com.moclojer.middleware.latency-test
  (:require [clojure.test :refer [deftest testing is]]
            [com.moclojer.middleware.latency :as latency]))

(deftest wrap-latency-test
  (testing "Basic latency simulation"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          request {:reitit.core/match
                  {:data {:latency {:min-ms 50
                                    :max-ms 100}}}}
          start-time (System/currentTimeMillis)
          response (wrapped-handler request)
          elapsed-time (- (System/currentTimeMillis) start-time)]

      (is (= 200 (:status response)))
      (is (<= 50 elapsed-time 100))))

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
                 (get-in (ex-data e) [:body :error])))))))

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
            (is (<= 100 elapsed-time)))))))

  (testing "No latency when not configured"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          request {:reitit.core/match {:data {}}}
          start-time (System/currentTimeMillis)
          response (wrapped-handler request)
          elapsed-time (- (System/currentTimeMillis) start-time)]

      (is (= 200 (:status response)))
      (is (< elapsed-time 50) "Should not add significant latency"))))