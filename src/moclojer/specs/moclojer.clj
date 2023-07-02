(ns moclojer.specs.moclojer
  (:require
   [io.pedestal.log :as log]))

(defn ->moclojer
  [spec]
  (log/info :mode "moclojer")
  spec)
