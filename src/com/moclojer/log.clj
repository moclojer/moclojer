(ns com.moclojer.log
  (:require [clojure.string :as string]
            [io.pedestal.interceptor.helpers :as interceptor]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as core-appenders]
            [taoensso.timbre.appenders.community.sentry :as sentry]
            [timbre-json-appender.core :as tas])
  (:import (java.util.logging
            Filter
            Formatter
            Handler
            LogRecord
            Logger)))

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

(def supported-log-fmts-cfg
  {:default
   {:appenders
    {:println (core-appenders/println-appender :auto)}}
   :json (tas/install)})

(defn log-format->mergeable-cfg
  [fmt]
  (get supported-log-fmts-cfg (or fmt :println)))

(defn clean-timbre-appenders []
  (->> (reduce-kv
        (fn [acc k _]
          (assoc acc k nil))
        {} (:appenders timbre/*config*))
       (assoc nil :appenders)
       timbre/merge-config!))

(defn setup
  "timbre setup for logging"
  [level fmt]
  (clean-timbre-appenders)
  (global-setup (.getParent (Logger/getGlobal))) ;; disable `org.eclipse.jetty` logs
  (let [config (merge
                {:min-level level
                 :ns-filter {:allow #{"com.moclojer.*"}}}
                (log-format->mergeable-cfg fmt))
        sentry-dsn (or (System/getenv "SENTRY_DSN") nil)]
    (timbre/merge-config! config)
    (when sentry-dsn
      (timbre/merge-config! {:appenders {:sentry (sentry/sentry-appender sentry-dsn)}}))))

(defmacro log
  "log macro for logging with timbre"
  [level & args]
  `(timbre/log! ~level :p ~args ~{:?line (:line (meta &form))}))

(def request
  "Log the request's method and uri."
  (interceptor/on-request
   ::log-request
   (fn [request]
     (log :info
          :method (string/upper-case (name (:request-method request)))
          :host (:server-name request)
          :uri (:uri request)
          :query-string (:query-string request))
     request)))
