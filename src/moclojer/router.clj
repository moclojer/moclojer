(ns moclojer.router
  (:require [clojure.data.json :as json]
            [moclojer.log :as log]
            [moclojer.specs.moclojer :as spec]
            [moclojer.specs.openapi :as openapi]))

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
  (let [mode (if mocks :openapi :moclojer)]
    (log/log :info :spec-mode :mode mode)
    (->> (if mocks
           (openapi/->moclojer config mocks)
           config)
         (cons home-endpoint)
         (spec/->pedestal))))
