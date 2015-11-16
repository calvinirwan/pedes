(ns pedes.server
  (:gen-class) ; for -main method in uberjar
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as server]
            [pedes.service :as service]
            [pedes.routes :as routes]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defrecord WebServer [server service]
  component/Lifecycle
  (start [this]
         (if server
           (do
             (println "server udah nyala baoss... lo mau ngerusak ??")
             this)
           (let [server (-> (:service-data service)
                            server/default-interceptors
                            server/dev-interceptors
                            server/create-server)]
             (do
               (server/start server)
               (-> this
                   (assoc :server server))))))
  (stop [this]
        (if-not server
          (do
            (println "server nya udah mateeeeee")
            this)
          (do
            (server/stop server)
            (-> this
                (assoc :server nil))))))

(defn make-web-server []
  (map->WebServer {}))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server..."))

;; If you package the service up as a WAR,
;; some form of the following function sections is required (for io.pedestal.servlet.ClojureVarServlet).

;;(defonce servlet  (atom nil))
;;
;;(defn servlet-init
;;  [_ config]
;;  ;; Initialize your app here.
;;  (reset! servlet  (server/servlet-init service/service nil)))
;;
;;(defn servlet-service
;;  [_ request response]
;;  (server/servlet-service @servlet request response))
;;
;;(defn servlet-destroy
;;  [_]
;;  (server/servlet-destroy @servlet)
;;  (reset! servlet nil))

