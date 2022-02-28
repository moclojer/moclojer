(ns moclojer.core
  (:gen-class)
  (:require [clojure.string :as string]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
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
  ([r]
   [(body-params/body-params)
    http/json-body
    (fn [req]
      {:status       (get-in r [:endpoint :response :status] 200)
       :content-type (get-in r [:endpoint :response :headers :content-type]
                             "application/json")
       :body         (selmer/render (get-in r [:endpoint :response :body] "{}")
                                    {:path-params  (:path-params req)
                                     :query-params (:query-params req)
                                     :json-params  (:json-params req)})})])
  ([r route-name]
   [(body-params/body-params)
    http/json-body
    (fn [req]
      {:status       (get-in r [route-name :response :status] 200)
       :content-type (get-in r [route-name :response :headers :content-type]
                             "application/json")
       :body         (selmer/render (get-in r [route-name :response :body] "{}")
                                    {:path-params  (:path-params req)
                                     :query-params (:query-params req)
                                     :json-params  (:json-params req)})})]))

(defmulti make-router (fn [config]
                        (when (= (get config "openapi") "3.0.0") :open-api)
                        (let [type (-> config ::config :type)]
                          (cond
                            (= type :edn) :edn
                            :else  :route))))

(defn get-endpoints-edn [config]
  (let [endpoints (:endpoints config)]
    endpoints))

(defmethod make-router :edn
  [{::keys [config]}]
  (sequence (mapcat (fn [{:keys [endpoint]
                      :as   r}]
                      (let [route-name (first r)]
                        (route/expand-routes
                          #{[(:path endpoint)
                             (keyword (string/lower-case (:method endpoint "get")))
                             (handler r route-name)
                             :route-name route-name]})))
                    (get-endpoints-edn config))))

(defmethod make-router :open-api
  [{::keys [config]}]
  (openapi/generate-pedestal-route config))

(defmethod make-router :route
  [{::keys [config]}]
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
               config)))

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
