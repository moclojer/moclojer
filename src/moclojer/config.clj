(ns moclojer.config)

;; https://specifications.freedesktop.org/basedir-spec/latest/ar01s03.html

(def get-home (System/getProperty "user.home"))

(def get-xdg-config-home
  "Get the XDG_CONFIG_HOME or HOME/.config if its not defined."
  (or (System/getenv "XDG_CONFIG_HOME")
      (str get-home "/.config")))

(defn with-xdg
  "Will prefix with the XDG home."
  [s] (str get-xdg-config-home "/" s))
