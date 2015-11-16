(ns pedes.service
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [ring.util.response :as ring-resp]
            [ring.handler.dump :refer [handle-dump]]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.test :as ptest]
            [io.pedestal.http.csrf :as csrf] 
            [clj-http.client :as cl]
            [clj-http.cookies :refer [cookie-store]]
            [pedes.routes :refer [routes]]))

;; Consumed by pedes.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::bootstrap/interceptors []

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/enable-csrf {}
              ::bootstrap/enable-session {}
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 3000})

(defonce runnable-service
  (-> service ;; start with production configuration
      (merge {:env :dev
              ;; do not block thread that starts web server
              ::bootstrap/join? false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ;; all origins are allowed in dev mode
              ::bootstrap/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains

      ;; bootstrap/default-interceptors
      ;; bootstrap/dev-interceptors
      ;; bootstrap/create-server
      ))

(defrecord ServiceMap [service-data routes]
  component/Lifecycle
  (start [this]
         (let [service-map (assoc service-data
                                  ::bootstrap/routes
                                  (:routes routes))]
           (assoc this :service-data service-map)))
  (stop [this]
        (update-in this [:service-map ::bootstrap/routes] pop)))

(defn make-service-map
  ""
  []
  (map->ServiceMap {:service-data runnable-service}))
