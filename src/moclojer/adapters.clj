(ns moclojer.adapters)

(defn inputs->config
  [{:keys [args opts]} envs current-version]
  (let [{:keys [c config m mocks v version h help]} (first args)]
    {:current-version current-version
     :config-path (or c config (:config envs) (:config opts))
     :mocks-path (or m mocks (:mocks envs) (:mocks opts))
     :version (or v version (:version opts))
     :help (or h help (:help opts))}))
