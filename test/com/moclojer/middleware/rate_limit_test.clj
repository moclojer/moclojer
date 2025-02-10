(ns com.moclojer.middleware.rate-limit-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [com.moclojer.middleware.rate-limit :as rate-limit]))

;; Reset the store before each test
(use-fixtures :each
  (fn [f]
    (reset! @#'rate-limit/requests-store {})
    (f)))

(deftest wrap-rate-limit-test
  (testing "Basic rate limiting"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (rate-limit/wrap-rate-limit handler)
          request {:reitit.core/match
                   {:data {:rate-limit {:window-ms 1000
                                        :max-requests 2
                                        :key-fn (constantly "test-key")}}}}]

      ;; First request should pass
      (let [response (wrapped-handler request)]
        (is (= 200 (:status response)))
        (is (= "2" (get-in response [:headers "X-RateLimit-Limit"])))
        (is (= "1" (get-in response [:headers "X-RateLimit-Remaining"]))))

      ;; Second request should pass
      (let [response (wrapped-handler request)]
        (is (= 200 (:status response)))
        (is (= "0" (get-in response [:headers "X-RateLimit-Remaining"]))))

      ;; Third request should be blocked
      (let [response (wrapped-handler request)]
        (is (= 429 (:status response)))
        (is (= "Rate limit exceeded" (get-in response [:body :error]))))))

  (testing "Different keys should have separate limits"
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (rate-limit/wrap-rate-limit handler)
          make-request #(hash-map :reitit.core/match
                                  {:data {:rate-limit {:window-ms 1000
                                                       :max-requests 1
                                                       :key-fn :key}}}
                                  :key %)]

      ;; Request from key1 should pass
      (is (= 200 (:status (wrapped-handler (make-request "key1")))))

      ;; Request from key2 should pass
      (is (= 200 (:status (wrapped-handler (make-request "key2")))))

      ;; Second request from key1 should be blocked
      (is (= 429 (:status (wrapped-handler (make-request "key1"))))))))