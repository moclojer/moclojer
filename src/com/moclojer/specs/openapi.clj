(ns com.moclojer.specs.openapi
  (:require
   [clojure.string :as str]))

(defn convert-path
  "Converts OpenAPI path to moclojer path.

  Example: /pets/{id} -> /pets/:id"
  [path]
  (str "/" (str/replace
            (name path)
            #"\{([^\}]+)\}" ":$1")))

(defn ->moclojer
  "Converts OpenAPI spec to moclojer spec.

  Example:
    Input: {:paths {'/pets' {:get {...}}}}
    Output: [{:endpoint {:method 'GET' :path '/pets' ...}}]"
  [{:keys [paths]} mocks]
  (->>
   (for [[path methods] paths]
     (for [[method {:keys [operationId]}] methods]
       (let [path (convert-path path)]
         {:endpoint {:method (str/upper-case (name method))
                     :path path
                     :response (get mocks (keyword operationId))}})))
   (mapcat identity)))
