(ns moclojer.handler
  (:require [clojure.data.json :as json]
            [clojure.string :as string]))

(def home-endpoint
  {:endpoint {:method      :get
              :path        "/"
              :router-name ::moclojer
              :response    {:headers {}
                            :body    (json/write-str {:body (str '(-> moclojer server))})
                            :status  200}}})

(defn moclojer->openapi
  [endpoints]
  {"openapi" "3.0.0"
   "paths" (into {}
                 (for [[path ops] (group-by :path (map :endpoint endpoints))]
                   [(string/replace path
                                    #"(:[^/]{0,})"
                                    (fn [[v]]
                                      (str "{" (subs v 1) "}")))
                    (into {}
                          (for [{:keys [method response router-name]} ops
                                :let [{:keys [status headers body]} response]]
                            [(string/lower-case (name method))
                             (merge {"x-mockResponse" {"body"    body
                                                       "status"  status
                                                       "headers" (into {}
                                                                       (map (fn [[k v]]
                                                                              [(name k) (str v)]))
                                                                       headers)}}
                                    (when router-name
                                      {"operationId" (name router-name)}))]))]))})
