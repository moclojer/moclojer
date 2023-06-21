(ns moclojer.specs.moclojer
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.pedestal.http.route :as route]
            [io.pedestal.log :as log]
            [moclojer.handler :as handler]
            [yaml.core :as yaml])
  (:import (java.io FileNotFoundException)))

(defn open-config
  "open file"
  [path]
  (if (empty? path)
    (log/error :open-config "file not found"))
  (try
    (if (string/ends-with? path ".edn")
      (edn/read-string (str "[" (slurp path) "]"))
      (yaml/from-file path))
    (catch FileNotFoundException e
      (log/error :open-config (str "file not found" e)))))

(defn home
  [r]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (str '(-> moclojer server))})

(defn generate-routes
  "generate route from moclojer spec"
  [endpoints]
  (log/info :mode "moclojer")
  (let [handlers []]
    ;; (conj handlers (handler/home-endpoint)¡)
    (for [[groups ops] (group-by (juxt :host :path :method) (remove nil? (map :endpoint endpoints)))]
      (let [host (or (nth groups 0) "localhost")
            path (nth groups 1)
            method (or (string/lower-case (nth groups 2)) "get")
            method-keyword (keyword (string/lower-case method))
            route-name (keyword (str method "-"
                                     host "-"
                                     (string/replace
                                      (string/replace path "/" "")
                                      ":" "--")))]
        ;; (conj routes {:host host})
        (for [{:keys [response]} ops]
          (conj
           handlers
           [path
            method-keyword
            (handler/generic-handler response)
            :route-name route-name]))))))
(comment
  (defn nio-home
    [request]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str '(-> moclojer server))})

  (defn about-page
    [request]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body "about page!"})

  (def routes1
    `[[{:host "localhoset"}
       ["/" {:get nio-home}
        ["/about" {:get about-page}]]]])

  (defn routes2 [host]
    `#{{:app-name host :host host :scheme :http}
       ["/" :get nio-home :route-name :nio-home]
       ["/about" :get about-page :route-name :about-page]})

  (def routes3
    (into
     []
     (concat
      (route/expand-routes (routes2 "localhost"))
      (route/expand-routes (routes2 "127.0.0.1")))))

  (defn routes [host path]
    #{{:host host :scheme :http}
      path})

  (defn expand
    [routes]
    (route/expand-routes routes)))

(defn generate-routes-by-file
  "generate route from file"
  [path]
  ;; (println :here (generate-routes (open-config path)))
  (println :router routes3)
  routes3)
