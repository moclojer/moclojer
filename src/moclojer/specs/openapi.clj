(ns moclojer.specs.openapi
  (:require [clojure.edn :as edn]
            [io.pedestal.log :as log]
            [moclojer.specs.moclojer :as spec]))

(defn ->moclojer
  "convert openapi spec to moclojer spec"
  [config mock]
  (log/info :mode "openapi")
  ;; TODO: implement conversion from openapi to moclojer spec
  (spec/generate-route (edn/read-string "[]")))
