(ns com.moclojer.router
  (:require [clojure.data.json :as json]
            [com.moclojer.log :as log]
            [com.moclojer.specs.moclojer :as spec]
            [com.moclojer.specs.openapi :as openapi]))

(def home-endpoint
  "initial/home endpoint URL: /"
  {:endpoint {:method :get
              :path "/"
              :router-name ::moclojer
              :response {:headers {}
                         :body (json/write-str (str '(-> moclojer server)))
                         :status 200}}})

(defn smart-router
  "Identifies configuration type (moclojer or openapi spec)"
  [{:keys [::config ::mocks]}]
  (let [mode (if mocks :openapi :moclojer)
        swagger? (mocks :swagger?)]
    (log/log :info :spec-mode :mode mode)
    ;; if swagger is anabled, then we need to make routes for reitit
    ;; first I will keep pedestal because it is a big change
    ;; then we will remove pedestal and keep reitit 
    (if swagger?
      (when-not mocks (spec/->reitit config))
      (->> (if mocks
             (openapi/->moclojer config mocks)
             config)
           (cons home-endpoint)
           (spec/->pedestal)))))
