(ns com.moclojer.router-reitit
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.specs.moclojer :refer [->reitit]]
   [yaml.core :as yaml]))

(deftest spec->reitit
  (testing "Converts a spec to a reitit router"
    (let [spec (yaml/from-file "test/com/moclojer/resources/moclojer.yml")]
      (is (= {} (->reitit spec))))))
;(#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"hello-v1\": \"hello world!\"\n}\n"])
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"hello\": \"{{path-params.username}}!\"\n}\n"])
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"path-params\": \"{{path-params.param1}}\",\n  \"query-params\": \"{{query-params.param1}}\"\n}\n"])
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"hello\": \"Hello, World!\"\n}\n"]) 
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"hello-v1\": \"hello world!\"\n}\n"])
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"hello-v1\": \"{{path-params.username}}!\",\n  \"sufix\": true\n}\n"])
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"username\": \"{{path-params.username}}\",\n  \"age\": {{path-params.age}}\n}\n"])
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"hello-v1\": \"{{path-params.username}}!\",\n  \"sufix\": false\n}\n"])
;#ordered/map ([:status 200] [:headers #ordered/map ([:Content-Type "application/json"])] [:body "{\n  \"project\": \"{{json-params.project}}\"\n}\n"]))
;
;(["/v1/hello/" {"get" {:summary "Generated from get-localhost-v1hello", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/hello/:username" {"get" {:summary "Generated from get-localhost-hello--username", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/with-params/:param1" {"get" {:summary "Generated from get-localhost-with-params--param1", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/hello-world" {"get" {:summary "Generated from get-localhost-hello-world", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/v1/hello" {"get" {:summary "Generated from get-localhost-v1hello", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/v1/hello/test/:username/with-sufix" {"get" {:summary "Generated from get-localhost-v1hellotest--usernamewith-sufix", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/multi-path-param/:username/more/:age" {"get" {:summary "Generated from get-localhost-multi-path-param--usernamemore--age", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/v1/hello/test/:username" {"get" {:summary "Generated from get-localhost-v1hellotest--username", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;["/first-post-route" {"post" {:summary "Generated from post-localhost-first-post-route", :handler #function[com.moclojer.specs.moclojer/generic-handler/fn--40648]}}]
  ;)
