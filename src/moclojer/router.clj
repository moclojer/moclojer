(ns moclojer.router
  (:require [moclojer.specs.moclojer :as spec]
            [moclojer.specs.openapi :as openapi]))

(defn smart-router
  "identifies configuration type (moclojer or openapi spec)"
  [envs]
  (let [config (:moclojer.router/config envs)
        mocks (:moclojer.router/mocks envs)]
    (if mocks
      (openapi/->moclojer config mocks)
      (spec/generate-route-by-file config))))
