(ns moclojer.helper
  (:require [clojure.string :as string]))

(def moclojer-version
  "moclojer version rendering constant"
  (string/replace (slurp "META-INF/MOCLOJER_VERSION") "\n" ""))
