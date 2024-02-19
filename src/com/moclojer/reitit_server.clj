(ns com.moclojer.reitit-server
  (:require
   [clojure.core.async :as a]
   [com.moclojer.adapters :as adapters]
   [com.moclojer.io-utils :refer [open-file]]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.dev.pretty :as pretty]
   [reitit.http :as http]
   [reitit.http.coercion :as coercion]
   [reitit.http.interceptors.exception :as exception]
   [reitit.http.interceptors.multipart :as multipart]
   [reitit.http.interceptors.muuntaja :as muuntaja]
   [reitit.http.interceptors.parameters :as parameters]
   [reitit.pedestal :as pedestal]
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]))

(defn make-router [routes]
  (pedestal/routing-interceptor
   (http/router
    (->
     (concat
      ["/swagger.json"
       {:get {:no-doc true
              :swagger {:info {:title "moclojer api"
                               :description "with pedestal & reitit-http"}}
              :handler (swagger/create-swagger-handler)}}]
      routes)
     (conj []))

    {;:reitit.interceptor/transform dev/print-context-diffs ;; pretty context diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
     :exception pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :interceptors [;; swagger feature
                           swagger/swagger-feature
                             ;; query-params & form-params
                           (parameters/parameters-interceptor)
                             ;; content-negotiation
                           ;;(muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                           (muuntaja/format-response-interceptor)
                             ;; exception handling
                           (exception/exception-interceptor)
                             ;; decoding request body
                           (muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                           (coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                           (coercion/coerce-request-interceptor)
                             ;; multipart
                           (multipart/multipart-interceptor)]}})

    ;; optional default ring handler (if no routes have matched)
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/create-resource-handler)
    (ring/create-default-handler))))

(defn start-server!
  [{:keys [config-path mocks-path]}]
  (let [enabled? (some-> (or (System/getenv "SWAGGER_ENABLED") false)
                         boolean)
        *router (adapters/generate-routes (open-file config-path)
                                          :mocks-path mocks-path
                                          :swagger? enabled?)
        router (make-router @*router)]
    #_(-> {:env :dev
           ::server/type :jetty
           ::server/port 3000
           ::server/join? false
       ;; no pedestal routes
           ::server/routes []
       ;; allow serving the swagger-ui styles & scripts from self
           ::server/secure-headers {:content-security-policy-settings
                                    {:default-src "'self'"
                                     :style-src "'self' 'unsafe-inline'"
                                     :script-src "'self' 'unsafe-inline'"}}}
          (server/default-interceptors)
      ;; use the reitit router
          (pedestal/replace-last-interceptor (make-router))
          (server/dev-interceptors)
          (server/create-server)
          (server/start))
    (println "server running in port 3000")))
