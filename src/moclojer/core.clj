(ns moclojer.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.jetty]
            [moclojer.router :as router]))

(defn -main
  "start moclojer server"
  [& _]
  (prn "(-> moclojer :start-server)")
  (let [routes (router/make-smart-router)]
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
