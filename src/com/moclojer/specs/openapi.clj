(ns com.moclojer.specs.openapi
  (:require [clojure.string :as str]))

(defn convert-path
  "converts OpenAPI path to moclojer path
  e.g.: /pets/{id} -> /pets/:id"
  [path]
  (str "/" (str/replace
            (name path)
            #"\{([^\}]+)\}" ":$1")))

(defn ->moclojer
  "converts OpenAPI spec to moclojer spec"
  [{:keys [paths]} mocks]
  (->>
   (for [[path methods] paths]
     (for [[method {:keys [operationId]}] methods]
       (let [path (convert-path path)]
         {:endpoint {:method (name method)
                     :path path
                     :response (get mocks (keyword operationId))}})))
   (mapcat identity)))
