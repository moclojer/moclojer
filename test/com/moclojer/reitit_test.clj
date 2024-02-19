(ns com.moclojer.reitit-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.moclojer.adapters :as adapters]
   [yaml.core :as yaml]
   [com.moclojer.router :as router]))

(deftest convert-yml-to-reitit-spec
  (testing "should convert yml to reitit spec"
    (let [route (router/smart-router
                 {::router/config (yaml/from-file "test/com/moclojer/resources/moclojer.yml")
                  ::router/mocks {:swagger? true}})]
      (is
       {}
       route))))

(comment
  (router/smart-router
   {::router/config (yaml/from-file "test/com/moclojer/resources/moclojer.yml")
    ::router/mocks {:swagger? true}})

;
  )
