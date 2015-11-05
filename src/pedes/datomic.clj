(ns pedes.datomic
  (:require [com.stuartsierra.component :as component]
            [clojure.core.reducers :as r]
            [datomic.api :as d]))
(def uri "datomic:free://localhost:4334/pedes")
(defn create-db [uri]  (d/create-database uri))
(def conn (d/connect uri))

(defn get-like [name](d/q '[:find ?p :in $ ?n :where [?e :person/name ?n]
			[?e :person/like ?p]] (d/db conn) name))
(def tx-result
  (d/transact
   conn
   [[:db/add
     (d/tempid :db.part/user)
     :db/doc
     "hello world"]]))

(defrecord Datomic [db-uri conn]
  component/Lifecycle
  (start [this]
         (loop [i 0]
           (if-let [conn (try (d/connect db-uri)
                              (catch Exception e))]
             (assoc this :conn conn)
             (do (create-db db-uri)
                 (recur (inc i))))))
  (stop [this]
        (assoc this :conn nil)))

(defn make-datomic
  [db-uri]
  (map->Datomic {:db-uri db-uri
                 :conn nil}))
