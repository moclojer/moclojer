(ns moclojer.specs.moclojer
  (:require [cheshire.core :as cheshire]
            [clojure.string :as string]
            [io.pedestal.http.route :as route]
            [moclojer.external-body.xlsx :as xlsx]
            [selmer.parser :as selmer]))

(defn body->str
  "convert body to string, if it is edn it will be converted to json->str"
  [body]
  (if (string? body)
    body
    (cheshire/generate-string body)))

(defn build-body
  "build body from response"
  [response]
  (let [external-body (:external-body response)]
    (if external-body
      (xlsx/->map (:path external-body) (:sheet-name external-body))
      (:body response))))

(defn generic-handler
  [response]
  (fn [request]
    {:body    (selmer/render (body->str (build-body response)) request)
     :status  (:status response)
     :headers (into
               {}
               (map (fn [[k v]]
                      [(name k) (str v)]))
               (:headers response))}))

(defn generate-route-name
  [host path method]
  (str method "-" (or host "localhost") "-" (string/replace (string/replace path "/" "") ":" "--")))

(defn generate-method [method]
  (-> (or method "get")
      name
      string/lower-case))

(defn ->pedestal
  "generate routes from moclojer spec to pedestal"
  [spec]
  (->>
   (for [[[host path method] endpoints] (group-by (juxt :host :path :method)
                                                  (remove nil? (map :endpoint spec)))]
     (let [method (generate-method method)
           route-name (generate-route-name host path method)
           response (:response (first endpoints))]
       (route/expand-routes
        #{{:host host}
          [path
           (keyword method)
           (generic-handler response)
           :route-name (keyword route-name)]})))
   (mapcat identity)))
