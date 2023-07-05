(ns moclojer.router
  (:require
   [io.pedestal.log :as log]
   [moclojer.specs.moclojer :as spec]
   [moclojer.specs.openapi :as openapi]))

(defn smart-router
  "identifies configuration type (moclojer or openapi spec)"
  [config mocks]
  (spec/->pedestal
   (if mocks
     (do
       (log/info :mode "openapi")
       (openapi/->moclojer config mocks))
     (do
       (log/info :mode "moclojer")
       config))))

