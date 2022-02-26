(ns moclojer.core
  (:gen-class)
  (:require [clojure.string :as string]
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty]
            [io.pedestal.http.route :as route]
            [moclojer.openapi :as openapi]
            [selmer.parser :as selmer]
            [slugify.core :refer [slugify]]
            [yaml.core :as yaml]))

(defn home-handler
  "home handler /"
  [_]
  {:status 200
   :body   "{\"body\": \"(-> moclojer server)\"}"})

(defn handler
  "prepare function to receive http request (handler)"
  [r]
  (fn [req]
    {:status       (get-in r [:endpoint :response :status] 200)
     :content-type (get-in r [:endpoint :response :headers :content-type]
                           "application/json")
     :body         (selmer/render (get-in r [:endpoint :response :body] "{}")
                                  (:path-params req))}))

(defn make-router
  [{::keys [config]}]
  (if (= (get config "openapi") "3.0.0")
    (openapi/generate-pedestal-route config)
    (concat
      (route/expand-routes `#{["/" :get home-handler :route-name :home]})
      (sequence (mapcat
                 (fn [{:keys [endpoint]
                       :as   r}]
                   (route/expand-routes
                    #{[(:path endpoint)
                       (keyword (string/lower-case (:method endpoint "get")))
                       (handler r)
                       :route-name (keyword (slugify (:path endpoint)))]})))
                config))))

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
