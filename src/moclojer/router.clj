(ns moclojer.router
  (:require
   [io.pedestal.log :as log]
   [moclojer.routes :as routes]
   [moclojer.specs.openapi :as openapi]))

(defn smart-router
  "identifies configuration type (moclojer or openapi spec)"
  [config mocks]
  (routes/generate-routes
   (if mocks
     (do
       (log/info :mode "openapi")
       (openapi/->moclojer config mocks))
     (do
       (log/info :mode "moclojer")
       config))))

