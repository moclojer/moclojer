(ns moclojer.edn-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [clojure.edn :as edn]
            [io.pedestal.test :refer [response-for]]
            [moclojer.core :as moclojer]))

(defn service-fn
  [config]
  (-> {::http/routes (moclojer/make-router {::moclojer/config config})}
      http/default-interceptors
      http/create-servlet
      ::http/service-fn))

(defn load-config
  "Given a filename, load & return a config file"
  [filename]
  (edn/read-string (slurp filename)))

(deftest dynamic-endpoint-edn
  (let [config (load-config "moclojer.edn")
        service-fn (service-fn config)]
    (is (= {:pets [{:name "Uber" :type "dog"} {:name "Pinpolho" :type "cat"}]}
           (-> service-fn
               (response-for :get "/pets")
               :body
               (json/parse-string true))))))
