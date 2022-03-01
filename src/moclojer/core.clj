(ns moclojer.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [clojure.data.json :as json]
            [io.pedestal.http.jetty]
            [moclojer.handler :as handler]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]))

(defmulti make-router (fn [config]
                        (when (= (get config "openapi") "3.0.0") :open-api)
                        (let [type (-> config ::config :type)]
                          (cond
                            (= type :edn) :edn
                            :else  :route))))


(defmethod make-router :edn
  [{::keys [config]}]
  (handler/generate-pedestal-route-from-edn config))

(defmethod make-router :open-api
  [{::keys [config]}]
  (openapi/generate-pedestal-route config))

(defmethod make-router :route
  [{::keys [config]}]
  (handler/generate-pedestal-route config))

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
