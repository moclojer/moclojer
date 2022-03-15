(ns moclojer.openapi
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.http.route :as route]
            [selmer.parser :as selmer])
  (:import (java.time Instant)))

(s/def ::mocks
  (s/map-of string?
    (s/map-of string? any?)))
(s/def ::openapi
  (s/map-of string? any?))
(def path-item->operation
  "Convert path item to http method"
  #{"get" "put" "post" "delete" "options" "head" "patch" "trace"})

;; TODO: JSON Pointer library
(defn json-pointer-escape-token
  "https://datatracker.ietf.org/doc/html/rfc6901"
  [s]
  (-> s
      (string/replace #"~" "~0")
      (string/replace #"/" "~1")))

(defn json-pointer-unescape-token
  "https://datatracker.ietf.org/doc/html/rfc6901"
  [s]
  (-> s
      (string/replace #"~1" "/")
      (string/replace #"~0" "~")))

(defn json-pointer->path
  "Turns JSON Ponter reprsentation into clojure 'path'.
  Example:
  (get-in {\"a\" {\"b\" 42}} (json-pointer->path \"/a/b\"))
  => 42"
  [pointer]
  (mapv json-pointer-unescape-token (rest (string/split pointer #"/"))))

(defn json-path->pointer
  "Turn a clojure sequence of strings into a JSON pointer"
  [coll]
  (string/join "/"
               (cons "" (map json-pointer-escape-token coll))))

(def generate-response
  "Generate a response object from a response object in the OpenAPI spec"
  {:name  ::generate-response
   :enter (fn [{::keys [operation]
                :keys  [request]
                :as    ctx}]
            (assoc ctx :response
                       (if-let [{:strs [status body headers]} (get operation "x-mockResponse")]
                         {:body    (selmer/render body request)
                          :headers headers
                          :status  status}
                         {:status 501})))})

(defn with-mocks
  "Generate a mock response for a given operation"
  [openapi mocks]
  (let [op->path (into {}
                       (mapcat (fn [[path path-item]]
                                 (for [[method operation] path-item
                                       :when              (contains? path-item->operation method)
                                       :when              (contains? operation "operationId")]
                                   {(get operation "operationId") ["paths" path method]})))
                       (get openapi "paths"))]
    (reduce-kv (fn [openapi pointer-or-operation mock]
                 (let [path (or (op->path pointer-or-operation)
                              (json-pointer->path pointer-or-operation))]
                   (assoc-in openapi (conj path "x-mockResponse")
                     mock)))
               openapi mocks)))

(defn openapi-path->pedestal-path
  "Convert a path from openapi to pedestal"
  [path]
  ;; TODO: Handle wildcards
  ;; https://github.com/OAI/OpenAPI-Specification/issues/291
  ;; https://datatracker.ietf.org/doc/html/rfc6570
  (string/replace path
    #"\{([^}]+)\}"
    (fn [x]
      (str ":" (second x)))))

(defn resolve-ref
  "Resolve a reference to a schema"
  [root {:strs [$ref]
         :as   object}]
  (if $ref
    (get-in root (json-pointer->path $ref))
    object))

(defn generate-pedestal-route
  "Generate a Pedestal route from an OpenAPI specification"
  [config]
  (sequence (mapcat
              (fn [[path path-item]]
                (sequence
                  (mapcat (fn [[method operation]]
                            (when (contains? path-item->operation method)
                              (route/expand-routes
                                #{[(openapi-path->pedestal-path path)
                                   (keyword method)
                                   (into []
                                     cat
                                     [[{:name  ::add-operation
                                        :enter (fn [ctx]
                                                 (assoc ctx
                                                   ::path path
                                                   ::method method
                                                   ::openapi config
                                                   ::path-item path-item
                                                   ::operation operation))}
                                       (body-params/body-params)]
                                      (when (get-in operation ["requestBody" "content" "multipart/form-data"])
                                        [(middlewares/multipart-params)])
                                      (when-let [dir (get-in operation ["x-mockResponse" "store"])]
                                        (.mkdirs (io/file dir))
                                        [{:name  ::save-all-multipart
                                          :enter (fn [ctx]
                                                   (locking dir
                                                     (let [now-str (str (Instant/now))
                                                           temp-dir (loop [n 19]
                                                                      (let [d (io/file dir (str "req"
                                                                                             (string/replace
                                                                                               (subs now-str
                                                                                                 0 n)
                                                                                               #"[^0-9T-]"
                                                                                               "_")))]
                                                                        (if (.exists d)
                                                                          (recur (inc n))
                                                                          (doto d
                                                                            (.mkdirs)))))]
                                                       (doseq [[k v] (-> ctx :request :multipart-params)
                                                               :let [target (io/file temp-dir k)]]
                                                         (if (:tempfile v)
                                                           (io/copy (:tempfile v) target)
                                                           (spit target v)))))
                                                   ctx)}])
                                      [generate-response]])
                                   :route-name (keyword (or (get operation "operationId")
                                                          (json-path->pointer [path method])))]}))))
                  (resolve-ref config path-item))))
    (get config "paths")))
