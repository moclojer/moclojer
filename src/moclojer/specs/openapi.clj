(ns moclojer.specs.openapi
  (:require
   [clojure.string :as str]
   [io.pedestal.log :as log]))

(defn convert-path
  "Converts OpenAPI path to Moclojer path.
  Example: /pets/{id} -> /pets/:id"
  [path]
  (str "/" (str/replace
            (name path)
            #"\{([^\}]+)\}" ":$1")))

(defn ->moclojer
  "Convert OpenAPI spec to Moclojer spec."
  [{:keys [paths]} mocks]
  (log/info :mode "OpenAPI")
  (->>
   (for [[path methods] paths]
     (for [[method {:keys [operationId]}] methods]
       (let [path (convert-path path)]
         {:endpoint {:method method
                     :path path
                     :response (get mocks (keyword operationId))}})))
   (mapcat identity)))
