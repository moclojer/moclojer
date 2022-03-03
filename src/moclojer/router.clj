(ns moclojer.router
  (:require [moclojer.openapi :as openapi]
            [clojure.edn :as edn]
            [yaml.core :as yaml]
            [clojure.string :as str]
            [moclojer.handler :as handler]))

(defn edn-router
  [{{config :endpoints} ::config}]
  (handler/generate-pedestal-edn-route config))

(defn router
  [{::keys [config]}]
  (if (= (get config "openapi") "3.0.0")
    (openapi/generate-pedestal-route config)
    (handler/generate-pedestal-route config)))

(defn make-edn-specs [config]
  {:endpoints (edn/read-string (format "[%s]" (slurp config)))
   :type :edn})

(defn parse []
  (let [config (or (System/getenv "CONFIG")
                                  "moclojer.yml")
        mocks (yaml/from-file (or (System/getenv "MOCKS")
                                  "mocks.yml"))]
    (if (str/includes? config ".edn")
      (make-edn-specs config)
      (-> config
          (yaml/from-file)
          (openapi/with-mocks mocks)))))

(defn smart-router [specs]
  (if (= (:type specs) :edn)
      (edn-router {::config specs})
      (router {::config specs})))

(defn make-smart-router []
  (smart-router (parse)))

