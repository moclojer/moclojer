(ns com.moclojer.middleware.chaos-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.moclojer.middleware.chaos :as chaos]))

(deftest random-chance-test
  (testing "Probability 0 should never return true"
    (is (not (chaos/random-chance? 0.0))))

  (testing "Probability 1 should always return true"
    (is (chaos/random-chance? 1.0))))

(deftest basic-latency-test
  (testing "Basic latency simulation"
    (let [handler (fn [_] {:status 200})
          wrapped (chaos/wrap-chaos handler)
          request {:reitit.core/match
                   {:data {:chaos
                           {:latency {:enabled true
                                      :min-ms 50
                                      :max-ms 150
                                      :probability 1.0}}}}}
          start-time (System/currentTimeMillis)
          _ (wrapped request)
          elapsed-time (- (System/currentTimeMillis) start-time)]
      (is (<= 50 elapsed-time 160)
          (format "Elapsed time (%d ms) should be between 50ms and 160ms" elapsed-time)))))

(deftest failure-latency-test
  (testing "Failure simulation"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (chaos/wrap-chaos handler)
          request {:reitit.core/match
                   {:data {:chaos
                           {:failures {:enabled true
                                       :probability 1.0}}}}}  ;; Always fail
          response (wrapped-handler request)]
      (is (= 500 (:status response)) "Should return 500 status")
      (is (= "Chaos Testing: Simulated failure"
             (get-in response [:body :error])) "Should return error message"))))

(deftest corruption-test
  (testing "Response corruption simulation"
    (let [handler (fn [_] {:status 200 :body {:data "test"}})
          wrapped (chaos/wrap-chaos handler)
          request {:reitit.core/match
                   {:data {:chaos
                           {:corruption {:enabled true
                                         :probability 1.0}}}}}
          response (wrapped request)]
      (is (true? (get-in response [:body :chaos_corruption]))))))

(deftest chaos-disabled-test
  (testing "Middleware should not affect response when disabled"
    (let [original-response {:status 200 :body {:data "test"}}
          handler (constantly original-response)
          wrapped (chaos/wrap-chaos handler)
          request {:reitit.core/match
                   {:data {:chaos
                           {:latency {:enabled false}
                            :failures {:enabled false}
                            :corruption {:enabled false}}}}}
          response (wrapped request)]
      (is (= original-response response)))))

(deftest multiple-effects-test
  (testing "Multiple effects can be enabled"
    (let [handler (fn [_] {:status 200 :body {:data "test"}})
          wrapped (chaos/wrap-chaos handler)
          request {:reitit.core/match
                   {:data {:chaos
                           {:latency {:enabled true
                                      :min-ms 50
                                      :max-ms 150
                                      :probability 1.0}
                            :corruption {:enabled true
                                         :probability 1.0}}}}}]

      ;; Testamos apenas a corrupção, que é determinística
      (let [response (wrapped request)]
        (is (true? (get-in response [:body :chaos_corruption]))
            "Should corrupt response"))

      ;; Testamos a latência separadamente
      (let [start-time (System/currentTimeMillis)
            _ (wrapped request)
            elapsed-time (- (System/currentTimeMillis) start-time)]
        (is (>= elapsed-time 50)
            (format "Should have minimum latency of 50ms, got %dms" elapsed-time))
        (is (<= elapsed-time 160)
            (format "Should not exceed 160ms, got %dms" elapsed-time))))))

(deftest no-config-test
  (testing "Middleware should not fail when there is no configuration"
    (let [original-response {:status 200 :body {:data "test"}}
          handler (constantly original-response)
          wrapped (chaos/wrap-chaos handler)
          request {}  ;; No chaos configuration
          response (wrapped request)]
      (is (= original-response response)))))