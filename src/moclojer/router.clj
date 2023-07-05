(ns moclojer.router
  (:require [clojure.data.json :as json]
            [io.pedestal.log :as log]
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
  "identifies configuration type (moclojer or openapi spec)"
  [{:keys [::config ::mocks]}]
  (->>
   (if mocks
     (do
       (log/info :mode "openapi")
       (openapi/->moclojer config mocks))
     (do
       (log/info :mode "moclojer")
       config))
   (cons home-endpoint)
   (spec/->pedestal)))

