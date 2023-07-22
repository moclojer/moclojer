(ns moclojer.log
  (:require [clojure.string :as string]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as core-appenders])
  (:import (java.util.logging Filter Formatter Handler LogRecord Logger)))

(set! *warn-on-reflection* true)

(defn global-setup
  "global setup for logging, clear `org.eclipse.jetty` logs"
  [^Logger logger]
  (doseq [^Handler handler (.getHandlers logger)]
    (.setFormatter
     handler
     (proxy [Formatter] []
       (format [^LogRecord log-record]
         (str (.getLevel log-record) " [" (.getLoggerName log-record) "] - "
              (.formatMessage ^Formatter this log-record) "\n"))))
    (.setFilter
     handler
     (reify Filter
       (isLoggable [_ record]
         (not (string/starts-with? (.getLoggerName record)
                                   "org.eclipse.jetty")))))))

(defn setup
  "timbre setup for logging"
  [level stream]
  (global-setup (.getParent (Logger/getGlobal))) ;; disable `org.eclipse.jetty` logs
  (timbre/merge-config!
   {:min-level level
    :ns-filter {:allow #{"moclojer.*"}}
    :appenders
    {:println (core-appenders/println-appender {:stream stream})}}))

(defmacro log
  "log macro for logging with timbre"
  [level & args]
  `(timbre/log! ~level :p ~args ~{:?line (:line (meta &form))}))
