(ns com.moclojer.middleware.latency-test
  (:require [clojure.test :refer [deftest testing is]]
            [com.moclojer.middleware.latency :as latency]))

(deftest wrap-latency-test
  (testing "Basic latency simulation"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          min-ms 50
          max-ms 100
          buffer-ms 10  ;; Adding a buffer to accommodate system variations
          request {:reitit.core/match
                   {:data {:latency {:min-ms min-ms
                                     :max-ms max-ms}}}}
          start-time (System/currentTimeMillis)
          response (wrapped-handler request)
          elapsed-time (- (System/currentTimeMillis) start-time)]

      (is (= 200 (:status response)))
      (is (<= (- min-ms buffer-ms) elapsed-time (+ max-ms buffer-ms))
          (format "Elapsed time %d should be between %d and %d (with buffer of %d ms)"
                  elapsed-time (- min-ms buffer-ms) (+ max-ms buffer-ms) buffer-ms))))

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
          timeout-ms 100
          buffer-ms 10  ;; Buffer para acomodar variações do sistema
          request {:reitit.core/match
                   {:data {:latency {:timeout-rate 1.0  ;; Always timeout
                                     :timeout-ms timeout-ms}}}}
          start-time (System/currentTimeMillis)]

      (try
        (wrapped-handler request)
        (is false "Should have thrown an exception")
        (catch clojure.lang.ExceptionInfo e
          (let [elapsed-time (- (System/currentTimeMillis) start-time)]
            (is (= 504 (:status (ex-data e))))
            (is (= "Gateway Timeout (simulated)"
                   (get-in (ex-data e) [:body :error])))
            (is (<= (- timeout-ms buffer-ms) elapsed-time)
                (format "Elapsed time %d should be at least %d (with buffer of %d ms)"
                        elapsed-time (- timeout-ms buffer-ms) buffer-ms)))))))

  (testing "No latency when not configured"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          request {:reitit.core/match {:data {}}}
          start-time (System/currentTimeMillis)
          response (wrapped-handler request)
          elapsed-time (- (System/currentTimeMillis) start-time)]

      (is (= 200 (:status response)))
      (is (< elapsed-time 50) "Should not add significant latency"))))

(deftest wrap-latency-with-mocks-test
  (testing "Basic latency simulation with mocked time"
    (let [sleep-counter (atom 0)
          sleep-time (atom 0)
          mock-sleep (fn [ms]
                       (swap! sleep-counter inc)
                       (reset! sleep-time ms))
          handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          min-ms 50
          max-ms 100
          request {:reitit.core/match
                   {:data {:latency {:min-ms min-ms
                                     :max-ms max-ms}}}}]

      (binding [latency/*sleep-fn* mock-sleep]
        (let [response (wrapped-handler request)]
          (is (= 200 (:status response)))
          (is (= 1 @sleep-counter) "Sleep should be called once")
          (is (<= min-ms @sleep-time max-ms)
              (format "Sleep time %d should be between %d and %d"
                      @sleep-time min-ms max-ms))))))

  (testing "Timeout simulation with mocked time"
    (let [sleep-counter (atom 0)
          sleep-time (atom 0)
          mock-sleep (fn [ms]
                       (swap! sleep-counter inc)
                       (reset! sleep-time ms))
          handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (latency/wrap-latency handler)
          timeout-ms 100
          request {:reitit.core/match
                   {:data {:latency {:timeout-rate 1.0
                                     :timeout-ms timeout-ms}}}}]

      (binding [latency/*sleep-fn* mock-sleep]
        (try
          (wrapped-handler request)
          (is false "Should have thrown an exception")
          (catch clojure.lang.ExceptionInfo e
            (is (= 504 (:status (ex-data e))))
            (is (= 1 @sleep-counter) "Sleep should be called once")
            (is (= timeout-ms @sleep-time)
                (format "Sleep time should be %d" timeout-ms))))))))