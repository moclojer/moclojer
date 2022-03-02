(ns moclojer.core
  (:gen-class)
  (:require [clojure.edn :as edn]
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]
            [clojure.string :as str]
            [moclojer.mrouter :as mrouter]))


(defn parse []
  (let [config (or (System/getenv "CONFIG")
                                  "moclojer.yml")
        mocks (yaml/from-file (or (System/getenv "MOCKS")
                                  "mocks.yml"))]
    (if (str/includes? config ".edn")
      (edn/read-string (slurp config))
      (-> config
          (yaml/from-file)
          (openapi/with-mocks mocks)))))


(defn -main
  "start moclojer server"
  [& _]
  (prn "(-> moclojer :start-server)")
  (let [routes (-> (parse)
                   (mrouter/smart-router))]
    (-> {:env                     :prod
         ::http/routes            routes
         ::http/type              :jetty
         ::http/join?             true
         ::http/container-options {:h2c? true}
         ::http/port              (or (some-> (System/getenv "PORT")
                                              Integer/parseInt)
                                      8000)}
        http/default-interceptors
        (update ::http/interceptors into [http/json-body])
        http/create-server
        http/start)))
