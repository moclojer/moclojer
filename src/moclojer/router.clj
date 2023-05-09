(ns moclojer.router
  (:require [moclojer.specs.moclojer :as spec]
            [moclojer.specs.openapi :as openapi]))

(defn smart-router
  "identifies configuration type (moclojer or openapi spec)"
  [{::keys [config mocks]}]
  (if mocks
    (openapi/->moclojer config mocks)
    (spec/generate-route-by-file config)))
