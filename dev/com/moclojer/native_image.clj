(ns com.moclojer.native-image
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))


(def initialize-at-build-time
  "list of classes to initialize at build time"
  (str
   "--initialize-at-build-time="
   (string/join
    ","
    ["java.security.SecureRandom"
     "org.yaml.snakeyaml.DumperOptions$FlowStyle"
     "org.yaml.snakeyaml.DumperOptions$ScalarStyle"
     "com.fasterxml.jackson.core.io.CharTypes"
     "com.fasterxml.jackson.core.JsonFactory"
     "com.fasterxml.jackson.core.io.SerializedString"
     "com.fasterxml.jackson.core.io.JsonStringEncoder"
     "com.fasterxml.jackson.core.JsonGenerator"
     "com.fasterxml.jackson.dataformat.cbor.CBORFactory"
     "com.fasterxml.jackson.dataformat.smile.SmileFactory"
     "org.apache.poi.hssf.usermodel.HSSFCellStyle"
     "org.eclipse.jetty.util.log.Log"
     "org.eclipse.jetty.util.StringUtil"
     "org.eclipse.jetty.util.component.AbstractLifeCycle"
     "org.eclipse.jetty.util.BufferUtil"
     "org.eclipse.jetty.util.Uptime"
     "org.eclipse.jetty.http2.hpack.HpackEncoder"
     "org.eclipse.jetty.http2.hpack.HpackContext$1"
     "org.eclipse.jetty.http2.hpack.Huffman"
     "org.eclipse.jetty.http2.hpack.HpackContext"
     "org.eclipse.jetty.http.PreEncodedHttpField$1"
     "org.eclipse.jetty.http.HttpHeader"
     "org.eclipse.jetty.http.DateGenerator"
     "org.eclipse.jetty.http.HttpVersion"
     "org.eclipse.jetty.http.HttpMethod"
     "org.eclipse.jetty.http.HttpScheme"
     "org.eclipse.jetty.http.PreEncodedHttpField"
     "org.eclipse.jetty.server.Response"
     "org.eclipse.jetty.util.ssl.SslContextFactory"
     "org.eclipse.jetty.util.log.JettyAwareLogger"
     "org.slf4j.LoggerFactory"
     "org.slf4j.jul.JDK14LoggerAdapter"
     "org.slf4j.jul.JDK14LoggerAdapter$1"
     "io.opentracing.util.GlobalTracer"])))

(defn prepare-files
  "Prepare files for native-image build"
  []
  (println "Building native image configuration files")
  (let [native-config-dir (io/file "target" "native-config")
        native-config-path (.getAbsolutePath native-config-dir)
        sdk-root (or (System/getenv "SDKROOT")
                     (try
                       (let [{:keys [exit out]} (shell/sh "xcrun" "--sdk" "macosx" "--show-sdk-path")]
                         (when (zero? exit)
                           (string/trim out)))
                       (catch Exception _ nil)))
        linker-args (concat (when sdk-root
                              [(str "-H:NativeLinkerOption=-L" (io/file sdk-root "usr/lib"))])
                            ["-H:NativeLinkerOption=-lz"])
        base-args ["-H:Name=moclojer"
                   "-Dio.pedestal.log.defaultMetricsRecorder=nil"
                   "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"
                   "-Dorg.slf4j.simpleLogger.log.org.eclipse.jetty.server=error"
                   "--allow-incomplete-classpath"
                   "--features=InitAtBuildTimeFeature"
                   "-H:+UnlockExperimentalVMOptions"]
        tail-args ["--enable-all-security-services"
                   initialize-at-build-time
                   (str "-H:ConfigurationFileDirectories=" native-config-path)
                   (str "-H:ReflectionConfigurationFiles=" (io/file native-config-dir "reflect-config.json"))
                   (str "-H:ResourceConfigurationFiles=" (io/file native-config-dir "resource-config.json"))
                   "-H:EnableURLProtocols=http,https"
                   "-H:DashboardDump=report/moclojer"
                   "-H:+ReportExceptionStackTraces"
                   "-H:+DashboardHeap"
                   "-H:+DashboardCode"
                   "-H:+DashboardBgv"
                   "-H:+DashboardJson"
                   "-O0" ;; TODO: remove this option when generating optimized builds for production
                   "--no-fallback"
                   "--verbose"]]
    ;; create native-image configuration file `filter.json`
    (spit (io/file "target" "filter.json")
          (json/write-str {:rules []}))

    ;; create native-image parameter file `@native-image-args`
    (spit (io/file "target" "native-image-args")
          (string/join "\n" (concat base-args linker-args tail-args)))
    ;; graalvm configuration directory
    (.mkdirs native-config-dir)
    ;; default configuration placeholders to avoid missing files when running native-image
    (doseq [[fname contents] [["reflect-config.json" "[]"]
                              ["resource-config.json" (json/write-str {:resources {:includes []}})]]]
      (let [file (io/file native-config-dir fname)]
        (when-not (.exists file)
          (spit file contents))))))
