(ns moclojer.core
  (:gen-class)
  (:require [clojure.string :as string]
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty]
            [io.pedestal.http.route :as route]
            [yaml.core :as yaml]))

;; TODO: JSON Pointer library
(def path-item->operation
  #{"get" "put" "post" "delete" "options" "head" "patch" "trace"})

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

(defn home-handler
  "home handler /"
  [_]
  {:status 200
   :body   "(-> moclojer server)"})

(defn handler
  "prepare function to receive http request (handler)"
  [r]
  (fn [_] {:status       (get-in r [:endpoint :response :status] 200)
           :content-type (get-in r [:endpoint :response :headers :content-type]
                                 "application/json")
           :body         (get-in r [:endpoint :response :body] "{}")}))

(def generate-response
  {:name  ::generate-response
   :enter (fn [{::keys [operation]
                :as    ctx}]
            (assoc ctx :response
                       (if-let [{:strs [status body headers]} (get operation "x-mockResponse")]
                         {:body    body
                          :headers headers
                          :status  status}
                         {:status 501})))})


(defn with-mocks
  [openapi mocks]
  (let [op->path (into {}
                       (mapcat (fn [[path path-item]]
                                 (for [[method operation] path-item
                                       :when (contains? path-item->operation method)
                                       :when (contains? operation "operationId")]
                                   {(get operation "operationId")
                                    ["paths" path method]})))
                       (get openapi "paths"))]
    (reduce-kv (fn [openapi pointer-or-operation mock]
                 (let [path (or (op->path pointer-or-operation)
                                (json-pointer->path pointer-or-operation))]
                   (assoc-in openapi (conj path "x-mockResponse")
                             mock)))
               openapi mocks)))


(defn openapi-path->pedestal-path
  [path]
  ;; TODO: Handle wildcards
  ;; https://github.com/OAI/OpenAPI-Specification/issues/291
  ;; https://datatracker.ietf.org/doc/html/rfc6570
  (string/replace path
                  #"\{([^}]+)\}"
                  (fn [x]
                    (str ":" (second x)))))

(defn resolve-ref
  [root {:strs [$ref]
         :as   object}]
  (if $ref
    (get-in root (json-pointer->path $ref))
    object))

(defn make-router
  [{::keys [config]}]
  (if (= (get config "openapi") "3.0.0")
    (sequence (mapcat
                (fn [[path path-item]]
                  (sequence
                    (mapcat (fn [[method operation]]
                              (when (contains? path-item->operation method)
                                (route/expand-routes
                                  #{[(openapi-path->pedestal-path path)
                                     (keyword method)
                                     [{:name  ::add-operation
                                       :enter (fn [ctx]
                                                (assoc ctx
                                                  ::path path
                                                  ::method method
                                                  ::openapi config
                                                  ::path-item path-item
                                                  ::operation operation))}
                                      generate-response]
                                     :route-name (keyword (or (get operation "operationId")
                                                              (json-path->pointer [path method])))]}))))
                    (resolve-ref config path-item))))
              (get config "paths"))
    (concat
      (route/expand-routes `#{["/" :get home-handler :route-name :home]})
      (sequence (mapcat
                  (fn [{:keys [endpoint] :as r}]
                    (route/expand-routes
                      #{[(:path endpoint)
                         (keyword (string/lower-case (:method endpoint "get")))
                         (handler r)
                         ;; TODO: slugify router-name -> remove replace `/`
                         :route-name (keyword (string/replace (:path endpoint) "/" ""))]})))
                config))))


(defn -main
  "start moclojer server"
  [& _]
  (prn "(moclojer :start-server)")
  (let [mocks (yaml/from-file (or (System/getenv "MOCKS")
                                  "mocks.yml"))
        spec (-> (yaml/from-file (or (System/getenv "CONFIG")
                                     "moclojer.yml"))
                 (with-mocks mocks))
        routes (make-router {::config spec})]
    (-> {:env                     :prod
         ::http/routes            routes
         ::http/type              :jetty
         ::http/join?             true
         ::http/container-options {:h2c? true}
         ::http/port              (or (some-> (System/getenv "PORT")
                                              Integer/parseInt)
                                      8000)}
        http/default-interceptors
        (update ::http/interceptors into [http/json-body])
        http/create-server
        http/start)))


