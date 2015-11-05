(ns pedes.system
  (:require [com.stuartsierra.component :as component]
            [pedes.datomic :as datomic]
            [pedes.server :as server]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]))

(def conf (read-string (slurp "config.edn")))

(defn init-system
  [conf]
  (component/system-map
   :datomic (datomic/make-datomic (:uri (:datomic conf)))
   :web-server (server/make-web-server)))

(def dev-system nil)

(defn init []
  (alter-var-root #'dev-system
                  (constantly (init-system conf))))


(defn start []
  (alter-var-root #'dev-system component/start))


(defn stop []
  (alter-var-root #'dev-system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'pedes.system/go))
