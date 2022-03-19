(ns moclojer.openapi
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.http.route :as route]
            [io.pedestal.log :as log]
            [json-schema.core :as js]
            [selmer.parser :as selmer])
  (:import (java.time Instant)))

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
                   (if-let [{:strs [status body headers]
                             :as   mock-response} (get operation "x-mockResponse")]
                     (merge
                       (when headers
                         {:headers headers})
                       (cond
                         (string? body) {:body (selmer/render body request)}

                         (contains? mock-response "body")
                         {:body (if (string/starts-with?
                                      (str (or
                                             (get headers "content-type")
                                             (get headers "Content-Type")))
                                      "applicantion/json")
                                  (json/generate-string body)
                                  body)}

                         :else {})
                       {:status status})
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

(def openapi-in->pedestal
  {"query"  :query-params
   "header" :headers
   "path"   :path-params
   ;; TODO
   #_#_"cookie" :cookie-params})

(defn try-number
  [s]
  (if (string? s)
    (try
      (let [n (edn/read-string s)]
        (if (number? n)
          n
          s))
      (catch Throwable ex
        s))
    s))

(def schema-check
  {:name  ::schema-check
   :enter (fn [{:keys  [request]
                ::keys [operation openapi path-item]
                :as    ctx}]
            (doseq [{:strs [name in required schema]} (concat
                                                        ;; TODO: If a parameter is already defined at the Path Item
                                                        ;; the new definition will override it but can never remove it.
                                                        (get path-item "parameters")
                                                        (get operation "parameters"))
                    :let [ident (get openapi-in->pedestal in)
                          kname (keyword name)
                          params (get request ident)
                          exists? (contains? params kname)]]
              (cond
                (and required
                  (not exists?))
                (throw (ex-info (str "Missing " (pr-str name) " parameter in " (pr-str in))
                         {:name   name
                          :in     in
                          :params params}))
                exists? (let [v (get params kname)
                              v (if (contains? #{"number" "integer"}
                                      (get schema "type"))
                                  (try-number v)
                                  v)]
                          (js/validate {"type"       "object"
                                        "properties" {name schema}}
                            {name v}))
                :else :ignore))
            ctx)
   :leave (fn [{:keys  [response]
                ::keys [operation openapi]
                :as    ctx}]
            (if-let [schema (get-in operation ["responses"
                                               (str (:status response))
                                               "content"
                                               (or (get (:headers response)
                                                     "Content-Type")
                                                 (get (:headers response)
                                                   "content-type"))
                                               "schema"])]
              (let [schema (merge schema
                             (select-keys openapi ["components"]))]
                (try
                  (js/validate schema
                    (:body response))
                  (catch Throwable ex
                    (log/info :exception
                      (doto ex
                        ;; stacktrace do not matter for our user
                        (.setStackTrace (into-array StackTraceElement []))))))
                ctx)
              ctx))})

(def route-error-handler
  {:name  ::route-error-handler
   :error (fn [ctx ex]
            ;; dev only
            ;; TODO: configure log/info to be present in final app
            ;; and log/error to be only dev-time
            #_(log/error :exception (ex-cause ex))
            (assoc ctx
              :response {:headers {"Content-Type" "application/json"}
                         :body    (json/generate-string {:error (ex-message (ex-cause ex))
                                                         :data  (ex-data (ex-cause ex))})
                         :status  400}))})


(defn save-all-multipart
  [dir]
  {:name  ::save-all-multipart
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
            ctx)})


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
                                      [[route-error-handler
                                        {:name  ::add-operation
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
                                         [(save-all-multipart dir)])
                                       [schema-check
                                        generate-response]])
                                :route-name (keyword (or (get operation "operationId")
                                                         (json-path->pointer [path method])))]}))))
                (resolve-ref config path-item))))
            (get config "paths")))
