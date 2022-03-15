(ns moclojer.router
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [io.pedestal.log :as log]
            [moclojer.handler :as handler]
            [moclojer.openapi :as openapi]
            [yaml.core :as yaml]))

(s/def ::config string?)
(s/def ::mocks (s/? string?))

(defn spec-ex
  [file value spec]
  (when-not (s/valid? spec value)
    (throw (ex-info (str "File " (pr-str file) " is do not conform with spec")
             {:file  file
              :spec  spec
              :value value}))))

(defn make-smart-router
  "Returns a pedestal routes"
  [{::keys [config mocks]}]
  (-> (if mocks
        (do
          (log/info :mode "openapi")
          (let [openapi (yaml/from-file config false)
                openapi-mocks (yaml/from-file mocks false)]
            (some-> (spec-ex config openapi ::openapi/openapi)
              throw)
            (some-> (spec-ex mocks openapi-mocks ::openapi/mocks)
              throw)
            (openapi/with-mocks openapi openapi-mocks)))
        (do
          (log/info :mode "moclojer")
          (let [endpoints (if (string/ends-with? config ".edn")
                            (edn/read-string (str "[" (slurp config) "]"))
                            (cons handler/home-endpoint
                              (yaml/from-file config)))]
            (some-> (spec-ex config endpoints ::handler/endpoints)
              throw)
            (handler/moclojer->openapi endpoints))))
    openapi/generate-pedestal-route))

(s/fdef make-smart-router
  :args (s/cat :env (s/keys :req [::config ::mocks])))