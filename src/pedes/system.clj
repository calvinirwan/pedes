(ns pedes.system
  (:require [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [com.stuartsierra.component :as component]
            [pedes.datomic :as datomic]
            [pedes.mail :as mail]
            [pedes.routes :as routes]
            [pedes.server :as server]
            ))

(def conf (read-string (slurp "config.edn")))

(defn init-system
  [conf]
  (component/system-map
   ;:datomic (datomic/make-datomic (:uri (:datomic conf)))
   :mail (mail/make-mail)
   :web-server (server/make-web-server)
   :routes (routes/make-routes)))

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
