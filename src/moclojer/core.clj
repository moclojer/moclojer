(ns moclojer.core
  (:require [clojure.string :as string]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [yaml.core :as yaml])
(:gen-class))

(defn home-handler [_]
  "home handler /"
  {:status 200
   :body   "(-> moclojer server)"})

(defn handler [r]
  "prepare function to receive http request (handler)"
  (fn [_] {:status       (get-in r [:endpoint :response :status] 200)
           :content-type (get-in r [:endpoint :response :headers :content-type]
                                 "application/json")
           :body         (get-in r [:endpoint :response :body] "{}")}))

(defn make-router [config]
  "gets configuration structure and makes url routes based on it"
  (with-local-vars
   [routers #{["/" :get home-handler :route-name :home]}]
    (doseq [r config]
      (var-set routers
               (conj @routers
                     [(get-in r [:endpoint :path])
                      (keyword (string/lower-case (get-in r [:endpoint :method] "get")))
                      (handler r)
                      ;; TODO: slugify router-name -> remove replace `/`
                      :route-name (keyword (string/replace (get-in r [:endpoint :path]) "/" ""))])))
    (route/expand-routes @routers)))

(defn -main [& _]
  "start moclojer server"
  (println "(moclojer :start-server)")
  (-> (http/create-server
       {:env         :prod
        ::http/routes (make-router
                       (yaml/from-file (or (System/getenv "CONFIG")
                                           "moclojer.yml")))
        ::http/type   :jetty
        ::http/join?  true
        ::http/port   (or (some-> (System/getenv "PORT")
                                  Integer/parseInt)
                          8000)})
      http/start))


