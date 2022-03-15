(ns moclojer.integration-test
  (:require [clojure.core.async :as async]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :refer [deftest is]]
            [yaml.core :as yaml])
  (:import (clojure.lang IDeref)
           (java.io BufferedReader Closeable File)
           (java.net URI)
           (java.net.http HttpClient HttpClient$Version HttpRequest HttpRequest$BodyPublishers HttpResponse$BodyHandlers)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)
           (java.util List)
           (java.util.concurrent TimeUnit)))

(set! *warn-on-reflection* true)

(comment
  (.delete (io/file "target" "moclojer.jar")))
(def *jar
  (delay
    (when-not (.exists (io/file "target" "moclojer.jar"))
      (apply (requiring-resolve `moclojer.build/-main) []))
    (.getAbsolutePath (io/file "target" "moclojer.jar"))))

(defn ^Closeable moclojer!
  [{::keys [files env]}]
  (let [dir (.toFile (Files/createTempDirectory "moclojer"
                       ^"[Ljava.nio.file.attribute.FileAttribute;" (into-array FileAttribute [])))
        _ (doseq [{::keys [name as value]} files]
            (spit (io/file dir name)
              (case as
                :json (json/write-str value)
                :yaml (yaml/generate-string value)
                (str value))))
        pb (-> (ProcessBuilder. ^List (mapv str ["java" "-jar" @*jar]))
             (doto
               (.directory dir)))
        _ (doto (.environment pb)
            (.putAll (into {} env)))
        p (.start pb)
        *out (atom [])
        watch! (fn [ident is]
                 (async/thread
                   (with-open [rdr (BufferedReader. (io/reader is))]
                     (loop []
                       (when-let [line (.readLine rdr)]
                         (swap! *out (fn [els]
                                       (println ident line)
                                       (conj els [ident line])))
                         (recur))))))]
    (watch! :out (.getInputStream p))
    (watch! :err (.getErrorStream p))
    (reify
      IDeref
      (deref [this]
        @*out)
      Closeable
      (close [this]
        (when-not (.isAlive p)
          (prn [:exited (.exitValue p)]))
        (.destroy p)
        (when-not (.waitFor p 1 TimeUnit/SECONDS)
          (.destroyForcibly p)
          (.waitFor p))
        (doseq [^File f (reverse (file-seq (io/file dir)))]
          (.delete f))))))


(defn wait-for-log
  [p re]
  (loop []
    (when-not (some (partial re-find re)
                (map second @p))
      (Thread/sleep 100)
      (recur))))

(def *client (delay (HttpClient/newHttpClient)))

(defn request
  [{:keys [server-port server-name uri scheme request-method protocol query-string
           #_remote-addr]
    :or   {scheme         :http
           request-method :get
           uri            "/"
           protocol       "HTTP/1.1"}}]
  (let [res (.send ^HttpClient @*client
              (-> (HttpRequest/newBuilder (URI. (name scheme) nil
                                            server-name
                                            server-port
                                            uri query-string nil))
                (.method (string/upper-case (name request-method))
                  (HttpRequest$BodyPublishers/ofString ""))
                (.version (case protocol
                            "HTTP/1.1" HttpClient$Version/HTTP_1_1))
                (.build))
              (HttpResponse$BodyHandlers/ofString))]
    {:status  (.statusCode res)
     :body    (.body res)
     :headers (into (sorted-map)
                (map (fn [[k v]]
                       [k (if (== 1 (count v))
                            (first v)
                            (vec v))]))
                (.map (.headers res)))}))

(deftest hello-simple-yaml
  (with-open [p (moclojer! {::files [{::name  "moclojer.yml"
                                      ::as    :yaml
                                      ::value [{:endpoint {:method   "GET"
                                                           :path     "/hello/:username"
                                                           :response {:status  200
                                                                      :headers {:Content-Type "application/json"}
                                                                      :body    (json/write-str {:hello "{{path-params.username}}!"})}}}]}]})]
    (wait-for-log p #"Started")
    (let [{:keys [status]} (request {:server-port 8000
                                     :server-name "moclojer.localhost"})]
      (is (= 200 status)))
    (let [{:keys [body status]} (request {:server-port 8000
                                          :uri         "/hello/moclojer"
                                          :server-name "moclojer.localhost"})]
      (is (= 200 status))
      (is (= {"hello" "moclojer!"}
            (json/read-str body))))))


(deftest hello-simple-openapi
  (with-open [p (moclojer! {::env   {"CONFIG" "openapi.yaml"
                                     "MOCKS"  "mocks.yaml"}
                            ::files [{::name  "openapi.yaml"
                                      ::as    :yaml
                                      ::value {"openapi"    "3.0.0",
                                               "paths"      {"/pets" {"get" {"summary"     "List all pets",
                                                                             "operationId" "listPets",
                                                                             "tags"        ["pets"],
                                                                             "parameters"  [{"name"        "limit",
                                                                                             "in"          "query",
                                                                                             "description" "How many items to return at one time (max 100)",
                                                                                             "required"    false,
                                                                                             "schema"      {"type" "integer", "format" "int32"}}],
                                                                             "responses"   {"200"     {"description" "A paged array of pets",
                                                                                                       "headers"     {"x-next" {"description" "A link to the next page of responses",
                                                                                                                                "schema"      {"type" "string"}}},
                                                                                                       "content"     {"application/json" {"schema" {"$ref" "#/components/schemas/Pets"}}}},
                                                                                            "default" {"description" "unexpected error",
                                                                                                       "content"     {"application/json" {"schema" {"$ref" "#/components/schemas/Error"}}}}}},},},
                                               "components" {"schemas" {"Pets"  {"type" "array", "items" {"$ref" "#/components/schemas/Pet"}},
                                                                        "Error" {"type"       "object",
                                                                                 "required"   ["code" "message"],
                                                                                 "properties" {"code"    {"type" "integer", "format" "int32"},
                                                                                               "message" {"type" "string"}}}}}}}
                                     {::name  "mocks.yaml"
                                      ::as    :yaml
                                      ::value {"listPets" {"status"  200
                                                           "body"    (json/write-str [{:id   0
                                                                                       :name "caramelo"}])
                                                           "headers" {"Content-Type" "application/json"}}}}]})]
    (wait-for-log p #"Started")
    (let [{:keys [body status]} (request {:server-port 8000
                                          :uri         "/pets"
                                          :server-name "moclojer.localhost"})]
      (is (= 200 status))
      (is (= [{"id"   0
               "name" "caramelo"}]
            (json/read-str body))))))



(deftest wrong-file-format
  (with-open [p (moclojer! {::env   {"CONFIG" "openapi.yaml"
                                     "MOCKS"  "mocks.yaml"}
                            ::files [{::name  "openapi.yaml"
                                      ::as    :yaml
                                      ::value {"openapi"    "3.0.0",
                                               "paths"      {"/pets" {"get" {"summary"     "List all pets",
                                                                             "operationId" "listPets",
                                                                             "tags"        ["pets"],
                                                                             "parameters"  [{"name"        "limit",
                                                                                             "in"          "query",
                                                                                             "description" "How many items to return at one time (max 100)",
                                                                                             "required"    false,
                                                                                             "schema"      {"type" "integer", "format" "int32"}}],
                                                                             "responses"   {"200"     {"description" "A paged array of pets",
                                                                                                       "headers"     {"x-next" {"description" "A link to the next page of responses",
                                                                                                                                "schema"      {"type" "string"}}},
                                                                                                       "content"     {"application/json" {"schema" {"$ref" "#/components/schemas/Pets"}}}},
                                                                                            "default" {"description" "unexpected error",
                                                                                                       "content"     {"application/json" {"schema" {"$ref" "#/components/schemas/Error"}}}}}},},},
                                               "components" {"schemas" {"Pets"  {"type" "array", "items" {"$ref" "#/components/schemas/Pet"}},
                                                                        "Error" {"type"       "object",
                                                                                 "required"   ["code" "message"],
                                                                                 "properties" {"code"    {"type" "integer", "format" "int32"},
                                                                                               "message" {"type" "string"}}}}}}}
                                     {::name  "mocks.yaml"
                                      ::as    :yaml
                                      ;; Wrong definition
                                      ::value [{:endpoint {:method   "GET"
                                                           :path     "/hello/:username"
                                                           :response {:status  200
                                                                      :headers {:Content-Type "application/json"}
                                                                      :body    (json/write-str {:hello "{{path-params.username}}!"})}}}]}]})]
    (wait-for-log p #"Started")
    (let [{:keys [body status]} (request {:server-port 8000
                                          :uri         "/pets"
                                          :server-name "moclojer.localhost"})]
      (is (= 404 status)))))
