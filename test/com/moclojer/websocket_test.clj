(ns com.moclojer.websocket-test
   (:require
    [clojure.data.json :as json]
    [clojure.test :refer [deftest is testing]]
    [com.moclojer.io-utils :refer [open-file]]
    [com.moclojer.specs.moclojer :as specs]))

 (deftest moclojer-websocket-config-test
   (testing "WebSocket configuration is correctly parsed from YAML"
     (let [config (open-file "test/com/moclojer/resources/moclojer.yml")
           websocket-config (first (filter #(contains? % :websocket) config))]

       (testing "WebSocket config has correct path"
         (is (= "/ws/echo" (get-in websocket-config [:websocket :path]))))

       (testing "WebSocket config has correct on-connect handler"
         (let [on-connect (get-in websocket-config [:websocket :on-connect])]
           (is (= "{\"status\": \"connected\", \"message\": \"Welcome to WebSocket Echo!\"}"
                  (:response on-connect)))))

       (testing "WebSocket config has correct on-message handlers"
         (let [on-message (get-in websocket-config [:websocket :on-message])]
           (is (= 2 (count on-message)))
           (is (= "ping" (get-in on-message [0 :pattern])))
           (is (= "pong" (get-in on-message [0 :response])))
           (is (= "{\"echo\": \"{{json-params.echo}}\"}" (get-in on-message [1 :pattern])))
           (is (= "{\"echoed\": \"{{json-params.echo}}\"}" (get-in on-message [1 :response]))))))))

 (deftest websocket-routing-test
   (testing "WebSocket routes are correctly generated"
     (let [config (open-file "test/com/moclojer/resources/moclojer.yml")
           routes (specs/->reitit config)]

       (testing "WebSocket route exists at correct path"
         (let [ws-route (first (filter #(= (first %) "/ws/echo") routes))]
           (is (not (nil? ws-route)))

           (testing "WebSocket route has GET handler"
             (is (contains? (get-in ws-route [1]) :get)))

           (testing "WebSocket handler is provided"
             (is (fn? (get-in ws-route [1 :get :handler])))))))))