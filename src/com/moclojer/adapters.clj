(ns com.moclojer.adapters
  (:require [com.moclojer.io-utils :refer [open-file]]
            [com.moclojer.router :as router]))

(defn inputs->config
  [{:keys [args opts]} envs]
  (let [{:keys [c config m mocks v version h help]} (first args)]
    {:config-path (or c config (:config envs) (:config opts))
     :mocks-path (or m mocks (:mocks envs) (:mocks opts))
     :version (or v version (:version opts))
     :help (or h help (:help opts))}))

(defn generate-routes
  "generate routes from config and mocks (not required)"
  [config & {:keys [mocks-path swagger?] :or {mocks-path nil
                                              swagger? false}}]
  (atom (router/smart-router {::router/config config
                              ::router/swagger? swagger?
                              ::router/mocks  (open-file mocks-path)})))
