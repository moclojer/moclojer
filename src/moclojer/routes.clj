(ns moclojer.routes
  (:require
   [cheshire.core :as cheshire]
   [clojure.string :as string]
   [io.pedestal.http.route.definition.table :as table]
   [selmer.parser :as selmer]))

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

(defn generate-route-name
  [host path method]
  (str method "-" host "-" (string/replace (string/replace path "/" "") ":" "--")))

(defn generate-routes
  "generate routes from moclojer spec"
  [spec]
  (->>
   (for [[[host path method] endpoints] (group-by (juxt :host :path :method)
                                                  (remove nil? (map :endpoint spec)))]
     (let [host (or host "localhost")
           method (or (string/lower-case method) "get")
           method-keyword (keyword (string/lower-case method))
           route-name (keyword (generate-route-name host path method))
           response (:response (first endpoints))]
       [path
        method-keyword
        (generic-handler response)
        :route-name route-name]))
   (table/table-routes)))
