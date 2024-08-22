(ns com.moclojer.config
  (:require [babashka.cli :as cli]))

(def version
  "0.3.3.1")

;; https://specifications.freedesktop.org/basedir-spec/latest/ar01s03.html
(def home-path (System/getProperty "user.home"))

(def xdg-config-home
  "Get the XDG_CONFIG_HOME environment variable or default to HOME/.config if it's not defined."
  (or (System/getenv "XDG_CONFIG_HOME")
      (str home-path "/.config")))

(defn with-xdg
  "Will prefix with the XDG home."
  [s] (str xdg-config-home "/" s))

(def moclojer-environment
  "Defines the Moclojer environment (:prod or :dev) that the application will run under."
  (or (keyword (System/getenv "MOCLOJER_ENV"))
      :prod))

(def spec
  "Configuration Parameters"
  {:config {:ref     "<file>"
            :desc    "Config path <file> or the CONFIG enviroment variable."
            :alias   :c
            :default "moclojer.yml"}
   :mocks  {:ref     "<file>"
            :desc    "OpenAPI v3 mocks path <file> or the MOCKS enviroment variable."
            :alias   :m}
   :version {:desc   "Show version."
             :alias  :v}
   :log-format {:ref     "<log-format>"
                :desc    "Log output format."
                :alias   :l
                :default "default"}
   :help    {:desc   "Show this Help."
             :alias  :h}})

(def empty-args
  "Args are empty"
  (str "moclojer (" version "), simple and efficient HTTP mock server.\r\n"
       (cli/format-opts {:spec spec :order [:config :mocks :version :help]})))
