(ns com.moclojer.router
  (:require
   [clojure.data.json :as json]
   [com.moclojer.log :as log]
   [com.moclojer.specs.moclojer :as spec]
   [com.moclojer.specs.openapi :as openapi]))

(def ^:private home-endpoint
  "Prebuilt endpoint for `/`."
  {:endpoint {:method :get
              :path "/"
              :router-name ::moclojer
              :response {:headers {}
                         :body (json/write-str '(-> moclojer server))
                         :status 200}}})

(defn smart-router
  "Given a list of mock endpoints that comply to either our
  in-house moclojer spec or openapi's spec, builds a generic
  reitit route that can be used later by the webserver.

  See also: `com.moclojer.adapters`."
  [{:keys [::config ::mocks]}]
  (let [mode (if mocks :openapi :moclojer)
        routes (if (seq mocks)
                 (if (vector? mocks)
                   mocks
                   (openapi/->moclojer config mocks))
                 config)]

    (log/log :info :spec-mode :mode mode)
    (->> routes
         (cons home-endpoint)
         (spec/->reitit)
         vec)))
