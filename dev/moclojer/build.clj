(ns moclojer.build
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.build.api :as b]
            [moclojer.config :as config]))

(def class-dir "target/classes")
(def uber-file "target/moclojer.jar")

(set! *warn-on-reflection* true)

(defn -main
  [& _]
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/delete {:path "target"})
    (b/write-pom {:class-dir class-dir
                  :lib       'moclojer/moclojer
                  :version   config/version
                  :basis     basis
                  :src-dirs  (:paths basis)})
    (b/compile-clj {:basis     basis
                    :src-dirs  (:paths basis)
                    :class-dir class-dir})
    (b/uber {:class-dir class-dir
             :main      'moclojer.core
             :uber-file uber-file
             :basis     basis})

    (.mkdirs (io/file "target" "native"))

    ;; create native-image configuration file `filter.json`
    (spit (io/file "target" "native" "filter.json")
          (json/write-str {:rules []}))

    ;; create native-image parameter file `@native-image-args`
    (spit (io/file "target" "native" "native-image-args")
          (string/join "\n" ["-H:Name=moclojer"
                             "-Dio.pedestal.log.defaultMetricsRecorder=nil"
                             "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"
                             "-Dorg.slf4j.simpleLogger.log.org.eclipse.jetty.server=error"
                             "--allow-incomplete-classpath"
                             "--features=clj_easy.graal_build_time.InitClojureClasses"
                             "--enable-all-security-services"
                             "--initialize-at-run-time=java.security.SecureRandom"
                             "--initialize-at-run-time=org.apache.poi.util.RandomSingleton"
                             "--initialize-at-run-time=com.fasterxml.jackson.core.io.SerializedString"
                             "--initialize-at-run-time=sun.security.ssl.SSLContextImpl"
                             "--trace-object-instantiation=org.yaml.snakeyaml.DumperOptions$ScalarStyle"
                             "--trace-object-instantiation=org.yaml.snakeyaml.DumperOptions$FlowStyle"
                             "--initialize-at-build-time=org.yaml.snakeyaml.DumperOptions$FlowStyle"
                             "--initialize-at-build-time=org.yaml.snakeyaml.DumperOptions$ScalarStyle"
                             "--trace-class-initialization=org.eclipse.jetty.server.Response,org.eclipse.jetty.util.Uptime,org.eclipse.jetty.http.HttpVersion,org.eclipse.jetty.util.component.AbstractLifeCycle,org.apache.poi.hssf.usermodel.HSSFCellStyle,org.eclipse.jetty.util.ssl.SslContextFactory,org.eclipse.jetty.http.HttpScheme,org.eclipse.jetty.http.HttpHeader,org.eclipse.jetty.util.BufferUtil,org.eclipse.jetty.util.StringUtil,com.fasterxml.jackson.core.JsonGenerator,org.eclipse.jetty.http2.hpack.HpackContext,com.fasterxml.jackson.dataformat.smile.SmileFactory,org.eclipse.jetty.http2.hpack.HpackContext$1,org.eclipse.jetty.http.HttpMethod,io.opentracing.util.GlobalTracer,org.eclipse.jetty.util.log.Log,org.eclipse.jetty.http.DateGenerator,com.fasterxml.jackson.dataformat.cbor.CBORFactory,org.eclipse.jetty.util.log.JettyAwareLogger,com.fasterxml.jackson.core.io.SerializedString,org.eclipse.jetty.http2.hpack.Huffman,com.fasterxml.jackson.core.JsonFactory,org.eclipse.jetty.http.PreEncodedHttpField$1,java.security.SecureRandom,org.slf4j.LoggerFactory,org.eclipse.jetty.http2.hpack.HpackEncoder,org.eclipse.jetty.http.PreEncodedHttpField,com.fasterxml.jackson.core.io.CharTypes"
                             "-H:DashboardDump=report/moclojer"
                             "-H:+ReportExceptionStackTraces"
                             "-H:+DashboardHeap"
                             "-H:+DashboardCode"
                             "-H:+DashboardBgv"
                             "-H:+DashboardJson"
                             "-H:ReflectionConfigurationFiles=reflect-config.json"
                             "-H:ResourceConfigurationFiles=resource-config.json"
                             "--no-fallback"
                             "--verbose"]))))
