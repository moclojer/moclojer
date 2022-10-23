(ns moclojer.openapi
  (:require [cheshire.core :as cheshire]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.http.route :as route]
            [selmer.parser :as selmer])
  (:import (java.time Instant)))

(def path-item->operation
  "convert path item to http method"
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
  "turns JSON Ponter reprsentation into clojure 'path'.
  example:
  (get-in {\"a\" {\"b\" 42}} (json-pointer->path \"/a/b\"))
  => 42"
  [pointer]
  (mapv json-pointer-unescape-token (rest (string/split pointer #"/"))))

(defn json-path->pointer
  "turn a clojure sequence of strings into a JSON pointer"
  [coll]
  (string/join "/"
               (cons "" (map json-pointer-escape-token coll))))

(defn body->str
  "convert body to string, if it is edn it will be converted to json->str"
  [body]
  (if (string? body)
    body
    (-> body
        (cheshire/generate-string))))

(def generate-response
  "generate a response object from a response object in the OpenAPI spec"
  {:name  ::generate-response
   :enter (fn [{::keys [operation]
                :keys  [request]
                :as    ctx}]
            (assoc ctx :response
                   (if-let [{:strs [status body headers]} (get operation "x-mockResponse")]
                     {:body    (selmer/render (body->str body) request)
                      :headers headers
                      :status  status}
                     {:status 501})))})

(defn with-mocks
  "generate a mock response for a given operation"
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
  "convert a path from openapi to pedestal"
  [path]
  ;; TODO: Handle wildcards
  ;; https://github.com/OAI/OpenAPI-Specification/issues/291
  ;; https://datatracker.ietf.org/doc/html/rfc6570
  (string/replace path
                  #"\{([^}]+)\}"
                  (fn [x]
                    (str ":" (second x)))))

(defn resolve-ref
  "resolve a reference to a schema"
  [root {:strs [$ref]
         :as   object}]
  (if $ref
    (get-in root (json-pointer->path $ref))
    object))

(defn build-routes
  "dynamically build pedestal routes"
  [config path path-item method operation]
  ;; TODO: this function is too complex, we need to rewrite simplifying the
  ;; logic and splitting the implementation into smaller functions
  (route/expand-routes
    #{;; use the `host` declared in the configuration file
      {:host (get operation "host" nil)}
      [(openapi-path->pedestal-path path)
       ;; used method in keyword format, ex: `:get`
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
                             (let [now-str  (str (Instant/now))
                                   temp-dir (loop [n 19]
                                              (let [d (io/file dir
                                                               (str "req"
                                                                    (string/replace
                                                                      (subs now-str 0 n)
                                                                      #"[^0-9T-]"
                                                                      "_")))]
                                                (if (.exists d)
                                                  (recur (inc n))
                                                  (doto d (.mkdirs)))))]
                               (doseq [[k v] (-> ctx :request :multipart-params)
                                       :let  [target (io/file temp-dir k)]]
                                 (if (:tempfile v)
                                   (io/copy (:tempfile v) target)
                                   (spit target v)))))
                           ctx)}])
              [generate-response]])
       :route-name (keyword
                     (string/join "-" [(get operation "host" "nil")
                                       (or (get operation "operationId")
                                           (json-path->pointer [path method]))]))]}))

(defn generate-pedestal-route
  "generate a pedestal route from an openapi spec"
  [config]
  (sequence (mapcat
             (fn [[path path-item]]
               (sequence
                (mapcat (fn [[method operation]]
                          (when (contains? path-item->operation method)
                            (build-routes config path path-item method operation))))
                (resolve-ref config path-item))))
            (get config "paths")))
