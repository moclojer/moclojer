(ns com.moclojer.adapters
  (:require
   [com.moclojer.io-utils :refer [open-file]]
   [com.moclojer.router :as router]))

(defn inputs->config
  "Adapts input args to a compact config map, used later on by the
  remaining application modules."
  [{:keys [args opts]} envs]
  (let [{:keys [c config m mocks v version h help l log-format]} (first args)]
    {:config-path (or c config (:config envs) (:config opts))
     :mocks-path (or m mocks (:mocks envs) (:mocks opts))
     :version (or v version (:version opts))
     :help (or h help (:help opts))
     :log-format (keyword (or l log-format (:log-format opts)))}))

(defn generate-routes
  "Returns an atom of generated router for given mocks endpoints.

  These will be gathered from either the

  1. `:mocks-path` and `:config-path` from the config path;
  2. solely from the `:mocks-path` passed directly.

  It's important to remember that both `config-path` and `mocks-path`
  are merged during route creation."
  [config & {:keys [mocks-path] :or {mocks-path nil}}]
  (atom (router/smart-router {::router/config config
                              ::router/mocks (open-file mocks-path)})))
