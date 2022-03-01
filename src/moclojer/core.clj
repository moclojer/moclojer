(ns moclojer.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [moclojer.handler :as handler]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]
            [;; For native-image
             io.pedestal.http.jetty])
  (:import (;; For native-image
             org.eclipse.jetty.server HttpOutput)))

(defn make-router
  [{::keys [config]}]
  (if (= (get config "openapi") "3.0.0")
    (openapi/generate-pedestal-route config)
    (handler/generate-pedestal-route config)))

(defn -main
  "start moclojer server"
  [& _]
  (prn "(-> moclojer :start-server)")
  (let [mocks (yaml/from-file (or (System/getenv "MOCKS")
                                  "mocks.yml"))
        spec (-> (yaml/from-file (or (System/getenv "CONFIG")
                                     "moclojer.yml"))
                 (openapi/with-mocks mocks))
        routes (make-router {::config spec})]
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
