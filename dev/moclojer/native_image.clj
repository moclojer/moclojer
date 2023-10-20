(ns moclojer.native-image
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
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
      ;; create native-image configuration file `filter.json`
  (spit (io/file "target" "filter.json")
        (json/write-str {:rules []}))

      ;; create native-image parameter file `@native-image-args`
  (spit (io/file "target" "native-image-args")
        (string/join "\n" ["-H:Name=moclojer"
                           "-Dio.pedestal.log.defaultMetricsRecorder=nil"
                           "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"
                           "-Dorg.slf4j.simpleLogger.log.org.eclipse.jetty.server=error"
                           "--allow-incomplete-classpath"
                           "--features=clj_easy.graal_build_time.InitClojureClasses"
                           ;; TODO: Option 'EnableAllSecurityServices' is deprecated
                           "--enable-all-security-services"
                           initialize-at-build-time ;; lists managed in the native-image library
                           "-H:EnableURLProtocols=http,https"
                           "-H:DashboardDump=report/moclojer"
                           "-H:+ReportExceptionStackTraces"
                           "-H:+DashboardHeap"
                           "-H:+DashboardCode"
                           "-H:+DashboardBgv"
                           "-H:+DashboardJson"
                           "-H:ReflectionConfigurationFiles=reflect-config.json"
                           "-H:ResourceConfigurationFiles=resource-config.json"
                           ;; TODO: remove this option when generating optimized builds for production
                           "-O0"
                           "--no-fallback"
                           "--verbose"])))
