(ns moclojer.log
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as core-appenders]))

(defn setup [level stream]
  (timbre/merge-config!
   {:min-level level
    :ns-filter {:deny #{"*"}
                :allow #{"moclojer.*"}}
    :appenders
    {:println (core-appenders/println-appender {:stream stream})}}))

(defmacro log [level & args]
  `(timbre/log! ~level :p ~args ~{:?line (:line (meta &form))}))
