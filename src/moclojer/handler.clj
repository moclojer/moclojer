(ns moclojer.handler
  (:require [selmer.parser :as selmer]
            [cheshire.core :as cheshire]
            [clojure.string :as string]))

(defn body->str
  "convert body to string, if it is edn it will be converted to json->str"
  [body]
  (if (string? body)
    body
    (-> body
        (cheshire/generate-string))))

(defn generic-handler
  [response]
  (fn [request]
    (prn :fn-req request)
    {:body    (selmer/render (body->str (:body response)) request)
     :status  (:status response)
     :headers (into
               {}
               (map (fn [[k v]]
                      [(name k) (str v)]))
               (:headers response))}))

(defn build-route
  [host method path body status headers store]
  (let [hostname (or host "localhost")
        method-keyword (keyword (string/lower-case (or method "get")))
        route-name (keyword (str (name method-keyword) "-"
                                 hostname "-"
                                 (string/replace (string/replace path "/" "")
                                                 ":" "--")))]
    ;; TODO: scheme support for http/https (default to http)
    #_{:host hostname :scheme :http}
    [path
     method-keyword
     (generic-handler body status headers)
     :route-name route-name]))


;; (defn home-endpoint
;;   "initial/home endpoint URI: /"
;;   []
;;   (router/build
;;    nil
;;    "GET"
;;    "/"
;;    (str '(-> moclojer server))
;;    200
;;    {}
;;    nil))
