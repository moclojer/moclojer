(ns moclojer.router
  (:require
   [moclojer.routes :as routes]
   [moclojer.specs.moclojer :as moclojer-spec]
   [moclojer.specs.openapi :as openapi-spec]))

(defn smart-router
  "identifies configuration type (moclojer or openapi spec)"
  [config mocks]
  (routes/generate-routes
   (if mocks
     (openapi-spec/->moclojer config mocks)
     (moclojer-spec/->moclojer config))))

