(ns com.moclojer.adapters
  (:require [com.moclojer.io-utils :refer [open-file]]
            [com.moclojer.router :as router]))

(defn inputs->config
  [{:keys [args opts]} envs]
  (let [{:keys [c config m mocks v version h help l log-format]} (first args)]
    {:config-path (or c config (:config envs) (:config opts))
     :mocks-path (or m mocks (:mocks envs) (:mocks opts))
     :version (or v version (:version opts))
     :help (or h help (:help opts))
     :log-format (keyword (or l log-format (:log-format opts)))}))

(defn generate-routes
  "generate routes from config and mocks (not required)"
  [config & {:keys [mocks-path] :or {mocks-path nil}}]
  (atom (router/smart-router {::router/config config
                              ::router/mocks  (open-file mocks-path)})))
