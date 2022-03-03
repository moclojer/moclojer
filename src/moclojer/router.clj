(ns moclojer.router
  (:require [moclojer.openapi :as openapi]
            [clojure.edn :as edn]
            [yaml.core :as yaml]
            [clojure.string :as str]
            [moclojer.handler :as handler]))

(defn edn-router
  "Receive spec edn config
   Example: {::config {:ednpoints [{:endpoint {...}}, {:endpoint {...}}] :type :edn}}
   Then get the endpoints collection and returns an Pedestal route"
  [{{config :endpoints} ::config}]
  (handler/generate-pedestal-edn-route config))

(defn router
  "Receive config .yaml and return pedestal Router"
  [{::keys [config]}]
  (if (= (get config "openapi") "3.0.0")
    (openapi/generate-pedestal-route config)
    (handler/generate-pedestal-route config)))

(defn make-edn-specs
  "Receive the name of edn config and return a map with endpoints declaration and type edn

  Example: (make-edn-specs moclojure.edn) =>
  {:ednpoints [{:endpoint {...}}, {:endpoint {...}}] :type :edn}
  "
  [config]
  {:endpoints (edn/read-string (format "[%s]" (slurp config)))
   :type :edn})

(defn parse
  "It gets the config from envrioment and returns specs"
  []
  (let [config (or (System/getenv "CONFIG")
                                  "moclojer.yml")
        mocks (yaml/from-file (or (System/getenv "MOCKS")
                                  "mocks.yml"))]
    (if (str/includes? config ".edn")
      (make-edn-specs config)
      (-> config
          (yaml/from-file)
          (openapi/with-mocks mocks)))))

(defn smart-router
  "Receive a specs and return pedestal route"
  [specs]
  (if (= (:type specs) :edn)
      (edn-router {::config specs})
      (router {::config specs})))

(defn make-smart-router
  "Returns a pedestal routes"
  []
  (smart-router (parse)))
