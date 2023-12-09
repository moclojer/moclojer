(ns moclojer.adapters
  (:require [moclojer.io-utils :refer [open-file]]
            [moclojer.router :as router]))

(defn inputs->config
  [{:keys [args opts]} envs]
  (let [{:keys [c config m mocks v version h help]} (first args)]
    {:config-path (or c config (:config envs) (:config opts))
     :mocks-path (or m mocks (:mocks envs) (:mocks opts))
     :version (or v version (:version opts))
     :help (or h help (:help opts))}))

(defn generate-routes
  "generate routes from config and mocks (not required)"
  [config & {:keys [mocks-path] :or {mocks-path nil}}]
  (atom (router/smart-router {::router/config config
                              ::router/mocks  (open-file mocks-path)})))
