(ns moclojer.core
  (:gen-class)
  (:require [babashka.cli :as cli]
            [clojure.string :as string]
            [io.pedestal.log :as log]
            [moclojer.adapters :as adapters]
            [moclojer.config :as config]
            [moclojer.log :as logs]
            [moclojer.server :as server])
  (:import (java.util.logging Filter Formatter Handler LogRecord Logger)))

(set! *warn-on-reflection* true)

(defn setup-logger
  [^Logger logger]
  (doseq [^Handler handler (.getHandlers logger)]
    (.setFormatter handler (proxy [Formatter] []
                             (format [^LogRecord log-record]
                               (str
                                 (.getLevel log-record)
                                 " ["
                                 (.getLoggerName log-record)
                                 "] - "
                                 (.formatMessage ^Formatter this log-record)
                                 "\n"))))

    (.setFilter handler
      (reify Filter (isLoggable [this record]
                      (not (string/starts-with? (.getLoggerName record)
                             "org.eclipse.jetty")))))))

(defn -main
  "software entry point"
  {:org.babashka/cli {:collect {:args []}}}
  [& args]
  (setup-logger (.getParent (Logger/getGlobal)))
  ;; (logs/setup :info :auto)
  (let [args-opts (cli/parse-args args {:spec config/spec})
        envs {:config (or (System/getenv "CONFIG")
                          (config/with-xdg "moclojer.yml"))
              :mocks (System/getenv "MOCKS")}
        config (adapters/inputs->config args-opts envs config/version)]

    (when (:version config)
      (logs/log :error :version-not-found "moclojer" config/version)
      (System/exit 0))

    (when (:help config)
      (logs/log :error :empty-args :empty-config config/empty-args)
      (System/exit 0))

    (server/start config)))
