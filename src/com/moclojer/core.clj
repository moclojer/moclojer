(ns com.moclojer.core
  (:require [babashka.cli :as cli]
            [com.moclojer.adapters :as adapters]
            [com.moclojer.config :as config]
            [com.moclojer.log :as log]
            [com.moclojer.server :as server])
  (:gen-class))

(defn -main
  "software entry point"
  {:org.babashka/cli {:collect {:args []}}}
  [& args]
  (let [args-opts (cli/parse-args args {:spec config/spec})
        envs {:config (or (System/getenv "CONFIG")
                          (config/with-xdg "moclojer.yml"))
              :mocks (System/getenv "MOCKS")}
        config (adapters/inputs->config args-opts envs)]

    (log/setup (:format config) :info :auto)

    (when (:version config)
      (log/log :error :version-not-found "moclojer" config/version)
      (System/exit 0))

    (when (:help config)
      (log/log :error :empty-args :empty-config config/empty-args)
      (System/exit 0))

    (server/start-server-with-file-watcher! config)))
