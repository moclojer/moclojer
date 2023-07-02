(ns moclojer.core
  (:gen-class)
  (:require
   [babashka.cli :as cli]
   [clojure.core.async :as async]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [io.pedestal.http :as http]
   [io.pedestal.http.body-params :as body-params]
   [io.pedestal.http.jetty]
   [io.pedestal.http.ring-middlewares :as middlewares]
   [io.pedestal.log :as log]
   [moclojer.adapters :as adapters]
   [moclojer.config :as config]
   [moclojer.router :as router]
   [yaml.core :as yaml])
  (:import
   (java.io FileNotFoundException)
   (java.nio.file
    FileSystems
    Path
    StandardWatchEventKinds
    WatchEvent)
   (java.util Properties)
   (java.util.concurrent TimeUnit)
   (org.eclipse.jetty.server.handler.gzip GzipHandler)
   (org.eclipse.jetty.servlet ServletContextHandler)))

(defn context-configurator
  "http container options, active gzip"
  [^ServletContextHandler context]
  (let [gzip-handler (GzipHandler.)]
    (.addIncludedMethods gzip-handler (make-array String 0))
    (.setExcludedAgentPatterns gzip-handler (make-array String 0))
    (.setGzipHandler context gzip-handler))
  context)

(defn watch-service
  "watch spec file change to server reload
   used async/thread to not have time for server shutdown"
  [files on-change]
  (let [ws (.newWatchService (FileSystems/getDefault))
        kv-paths (keep (fn [x]
                         (when-let [path (some-> x
                                                 io/file
                                                 .toPath
                                                 .toAbsolutePath)]
                           [x path]))
                       files)
        roots (into #{}
                    (keep (fn [[_ ^Path v]]
                            (.getParent v)))
                    kv-paths)]
    (doseq [path roots]
      (.register ^Path path
                 ws (into-array [StandardWatchEventKinds/ENTRY_MODIFY
                                 StandardWatchEventKinds/OVERFLOW
                                 StandardWatchEventKinds/ENTRY_DELETE
                                 StandardWatchEventKinds/ENTRY_CREATE])))
    (async/thread
      (loop []
        (when-let [watch-key (.poll ws 1 TimeUnit/SECONDS)]
          (let [changed (keep (fn [^WatchEvent event]
                                (let [^Path changed-path (.context event)]
                                  (first (for [[k v] kv-paths
                                               :when (= v (.toAbsolutePath changed-path))]
                                           k))))
                              (.pollEvents watch-key))]
            (when (seq changed)
              (on-change (set changed))))
          (.reset watch-key))
        (recur)))))

(def *pom-info
  "pom file info load"
  (delay
    (let [p (Properties.)]
      (some-> "META-INF/maven/moclojer/moclojer/pom.properties"
              io/resource
              io/reader
              (->> (.load p)))
      p)))

(defn open-file [path]
  (if (empty? path)
    (log/error :open-config "file not found")
    (try
      (if (string/ends-with? path ".edn")
        (edn/read-string (str "[" (slurp path) "]"))
        (yaml/from-file path))
      (catch FileNotFoundException e
        (log/error :open-config (str "file not found" e))))))

(defn start
  "start moclojer server"
  [{:keys [current-version config mocks]}]
  (log/info
   "-> moclojer"
   :start-server
   :version current-version
   :config config
   :mocks mocks)
  (let [envs {::router/config config
              ::router/mocks  mocks}
        *router (atom (router/smart-router
                       (open-file config)
                       (open-file mocks)))]
    (watch-service
     (vals envs)
     (fn [changed]
       (log/info :changed changed)
       (reset! *router (router/smart-router
                        (open-file config)
                        (open-file mocks)))))
    (-> {:env                     :prod
         ::http/routes            @*router
         ::http/type              :jetty
         ::http/join?             true
         ::http/container-options {:h2c?                 true
                                   :context-configurator context-configurator}
         ::http/host              (or (System/getenv "HOST") "0.0.0.0")
         ::http/port              (or (some-> (System/getenv "PORT")
                                              Integer/parseInt)
                                      8000)}
        http/default-interceptors
        (update ::http/interceptors into [http/json-body
                                          (body-params/body-params)
                                          (middlewares/multipart-params)])
        http/create-server
        http/start)))

(def spec {:config {:ref     "<file>"
                    :desc    "Config path <file> or the CONFIG enviroment variable."
                    :alias   :c
                    :default "moclojer.yml"}
           :mocks  {:ref     "<file>"
                    :desc    "OpenAPI v3 mocks path <file> or the MOCKS enviroment variable."
                    :alias   :m}
           :version {:desc   "Show version."
                     :alias  :v}
           :help    {:desc   "Show this Help."
                     :alias  :h}})

(defn -main
  {:org.babashka/cli {:collect {:args []}}}
  [& args]
  (let [args-opts (cli/parse-args args {:spec spec})
        envs {:config (or (System/getenv "CONFIG")
                          (config/with-xdg "moclojer.yml"))
              :mocks (System/getenv "MOCKS")}
        current-version (or (get @*pom-info "version") "dev")
        config (adapters/inputs->config args-opts envs current-version)]

    (when (:version config)
      (println "moclojer" current-version)
      (System/exit 0))

    (when (:help config)
      (println
       (str "moclojer (" current-version "), simple and efficient HTTP mock server.\r\n"
            (cli/format-opts {:spec spec :order [:config :mocks :version :help]})))
      (System/exit 0))

    (start config)))
