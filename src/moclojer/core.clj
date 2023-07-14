(ns moclojer.core
  (:gen-class)
  (:require [babashka.cli :as cli]
            [io.pedestal.log :as log]
            [moclojer.adapters :as adapters]
            [moclojer.config :as config]
            [moclojer.server :as server]))

(defn -main
  "software entry point"
  {:org.babashka/cli {:collect {:args []}}}
  [& args]
  (let [args-opts (cli/parse-args args {:spec config/spec})
        envs {:config (or (System/getenv "CONFIG")
                          (config/with-xdg "moclojer.yml"))
              :mocks (System/getenv "MOCKS")}
        config (adapters/inputs->config args-opts envs config/version)]

    (when (:version config)
      (log/error "moclojer" config/version)
      (System/exit 0))

    (when (:help config)
      (log/error :empty-args config/empty-args)
      (System/exit 0))

    (server/start config)))
