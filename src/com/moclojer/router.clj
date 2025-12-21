(ns com.moclojer.router
  (:require
   [clojure.data.json :as json]
   [com.moclojer.log :as log]
   [com.moclojer.specs.moclojer :as spec]
   [com.moclojer.specs.openapi :as openapi]
   [com.moclojer.specs.postman :as postman]))

(def ^:private home-endpoint
  "Prebuilt endpoint for `/`."
  {:endpoint {:method :get
              :path "/"
              :router-name ::moclojer
              :response {:headers {}
                         :body (json/write-str '(-> moclojer server))
                         :status 200}}})

(defn- detect-spec-type
  "Detects the type of spec based on the structure.

  Returns one of: :postman, :openapi, :moclojer"
  [config]
  (cond
    ;; Postman Collection has :info and :item keys
    (and (:info config) (:item config))
    :postman

    ;; OpenAPI has :paths key
    (:paths config)
    :openapi

    ;; Default to moclojer native format
    :else
    :moclojer))

(defn smart-router
  "Given a list of mock endpoints that comply to either our
  in-house moclojer spec, openapi's spec, or postman collection,
  builds a generic reitit route that can be used later by the webserver.

  See also: `com.moclojer.adapters`."
  [{:keys [::config ::mocks]}]
  (let [spec-type (detect-spec-type config)
        mode (if mocks :openapi spec-type)
        routes (cond
                 ;; OpenAPI with mocks file
                 (and (= spec-type :openapi) (seq mocks))
                 (if (vector? mocks)
                   mocks
                   (openapi/->moclojer config mocks))

                 ;; Postman Collection
                 (= spec-type :postman)
                 (postman/->moclojer config)

                 ;; Moclojer native format
                 :else
                 config)]

    (log/log :info :spec-mode :mode mode)
    (->> routes
         (cons home-endpoint)
         (spec/->reitit)
         vec)))
