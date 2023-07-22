(ns moclojer.log
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as core-appenders]))

(defn setup [level stream]
  (timbre/set-min-level! level)
  (timbre/merge-config!
   {:appenders
    {:println
     (core-appenders/println-appender {:stream stream})}}))

(defmacro log [level & args]
  `(timbre/log! ~level :p ~args ~{:?line (:line (meta &form))}))
