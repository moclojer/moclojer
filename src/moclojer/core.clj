(ns moclojer.core
  (:require [clojure.string :as string]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [yaml.core :as yaml])
(:gen-class))

(defn home-handler [_]
  {:status 200
   :body   "(-> moclojer server)"})

(defn make-router [config]
  (with-local-vars
   [routers #{["/" :get home-handler :route-name :home]}]
    (doseq [r config]
      (var-set routers
               (conj @routers
                     [(get-in r [:endpoint :path])
                      (keyword (string/lower-case (get-in r [:endpoint :method] "get")))
                      (fn [_] {:status (get-in r [:response :status])
                               :body   (get-in r [:response :body])})
                      ;; home-handler
                      ;; TODO: slugify router-name -> remove replace `/`
                      :route-name (keyword (string/replace (get-in r [:endpoint :path]) "/" ""))])))
    (route/expand-routes @routers)))

(defn -main [& _]
  (http/start
   (http/create-server
    {::http/routes (make-router (yaml/from-file "moclojer.yml"))
     ::http/type   :jetty
     ::http/port   8000})))


