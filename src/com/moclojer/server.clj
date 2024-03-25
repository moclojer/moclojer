(ns com.moclojer.server
  (:require [com.moclojer.adapters :as adapters]
            [com.moclojer.config :as config]
            [com.moclojer.io-utils :refer [open-file]]
            [com.moclojer.log :as log]
            [com.moclojer.watcher :refer [start-watch]]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.jetty]
            [io.pedestal.interceptor :as i]
            [io.pedestal.interceptor.error :as error]
            [route-swagger.interceptor :as sw.int]
            [route-swagger.doc :as sw.doc]
            [ring.swagger.middleware :refer [stringify-error]]
            [clojure.set :as set])
  (:import (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (org.eclipse.jetty.servlet ServletContextHandler)))

(defn context-configurator
  "http container options, active gzip"
  [^ServletContextHandler context]
  (let [gzip-handler (GzipHandler.)]
    (.addIncludedMethods gzip-handler (make-array String 0))
    (.setExcludedAgentPatterns gzip-handler (make-array String 0))
    (.setGzipHandler context gzip-handler))
  context)


(def common-body
  (i/interceptor
   {:name  ::common-body
    :enter (fn [context]
             (update context :request set/rename-keys {:edn-params       :body-params
                                                       :json-params      :body-params
                                                       :transit-params   :body-params
                                                       :multipart-params :form-params}))}))


(def error-responses
  (sw.doc/annotate
   {:responses {400 {}
                500 {}}}
   (error/error-dispatch [ctx ex]
                         [{:interceptor ::sw.int/coerce-request}]
                         (assoc ctx :response {:status 400 :body (stringify-error (:error (ex-data ex)))})

                         [{:interceptor ::sw.int/validate-response}]
                         (assoc ctx :response {:status 500 :body (stringify-error (:error (ex-data ex)))}))))

(defn get-interceptors
  "get pedestal default interceptors"
  [service-map]
  (-> service-map
      http/default-interceptors
      (update ::http/interceptors into [http/not-found
                                        http/json-body
                                        error-responses
                                        (body-params/body-params)
                                        common-body
                                        (sw.int/coerce-request)
                                        (sw.int/validate-response)])))

(defn build-config-map
  "build pedestal config map"
  [*router & {:keys [http-host http-port join?]}]
  {:env                     :prod
   ::http/request-logger    log/request
   ::http/routes            (fn [] @*router)
   ::http/type              :jetty
   ::http/join?              join?
   ::http/allowed-origins   {:creds true
                             :allowed-origins (constantly true)}
   ::http/container-options {:h2c?                 true
                             :context-configurator context-configurator}
   ::http/host              http-host
   ::http/port              http-port})

(defn start-server!
  "start moclojer server"
  [*router & {:keys [start?
                     join?] :or {start? true
                                 join? true}}]
  (let [http-host (or (System/getenv "HOST") "127.0.0.1")
        http-port (or (some-> (System/getenv "PORT")
                              Integer/parseInt)
                      8000)
        http-start (if start? http/start ::http/service-fn)]
    (log/log :info
             :moclojer-start
             "-> moclojer"
             :start-server
             :host http-host
             :port http-port
             :url (str "http://" http-host ":" http-port)
             :version config/version)
    (->
     *router
     (build-config-map  {:http-host http-host
                         :http-port http-port
                         :join? join?})
     get-interceptors
     http/create-server
     http-start)))

(defn create-watcher [*router & {:keys [config-path mocks-path]}]
  (start-watch [{:file config-path
                 :event-types [:create :modify :delete]
                 :callback (fn [_event file]
                             (prn file config-path)
                             (when (and (= file config-path)
                                        (not (nil? config-path)))
                               (log/log :info :moclojer-reload :router file :config config-path)
                               (reset! *router
                                       @(adapters/generate-routes (open-file config-path)
                                                                  :mocks-path mocks-path))))}
                {:file mocks-path
                 :callback (fn [_event file]
                             (when (and (= file mocks-path)
                                        (not (nil? mocks-path)))
                               (log/log :info :moclojer-reload :router file :mock-path mocks-path)
                               (reset! *router
                                       @(adapters/generate-routes (open-file config-path)
                                                                  :mocks-path mocks-path))))
                 :event-types [:create :modify :delete]}]))

(defn start-server-with-file-watcher!
  "start moclojer server with file watcher"
  [{:keys [config-path mocks-path]}]
  (let [*router (adapters/generate-routes (open-file config-path)
                                          :mocks-path mocks-path)]

    (create-watcher *router {:config-path config-path :mocks-path mocks-path})
    (start-server! *router)))
