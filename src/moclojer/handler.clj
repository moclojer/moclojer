(ns moclojer.handler
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [io.pedestal.http.route :as route]))

(defn struct-handler
  [host method path body status headers store]
  (let [hostname (or host "localhost")
        method-keyword (keyword (string/lower-case (or method "get")))
        router-name (keyword (str method-keyword "-" hostname "-" path))]
    #_{:host hostname :scheme :http}
     [path
      method-keyword
      {:body    (json/write-str {:body body})
       :status  status
       :headers (into
                 {}
                 (map (fn [[k v]]
                        [(name k) (str v)]))
                 headers)}
      :route-name router-name])
      ;; TODO: scheme support for http/https (default to http)
  )

(defn home-endpoint
  "initial/home endpoint URI: /"
  []
  (route/expand-routes
   (struct-handler
    nil
    "GET"
    "/"
    (json/write-str {:body (str '(-> moclojer server))})
    200
    {}
    nil)))
