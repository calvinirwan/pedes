(ns pedes.component
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan <! <!! >! >!!
                                        put! take!
                                        go go-loop
                                        split mult tap pipe
                                        dropping-buffer] :as async]))

(defn a [] (load "component"))

(defrecord Planet [name size gayness])
(defrecord Bitch [name age])

(declare validate-same-currency)
(defrecord Currency [divisor sym desc])
(defrecord Money [amount ^Currency currency]
  java.lang.Comparable
  (compareTo [m1 m2]
             (validate-same-currency m1 m2)
             (compare (:amount m1) (:amount m2))))

(def currencies {:usd (->Currency 100 "USD" "US Dollars")
                 :eur (->Currency 100 "EUR" "Euro")})

(defn- validate-same-currency
  [m1 m2]
  (or (= (:currency m1) (:currency m2))
      (throw
       (ex-info "Currencies do not match"
                {:m1 m1 :m2 m2}))))

(defn =$
  ([m1] true)
  ([m1 m2] (zero? (.compareTo m1 m2)))
  ([m1 m2 & monies] (every? zero? (map #(.compareTo m1 %) (conj monies m2)))))

(defn +$
  ([m1] m1)
  ([m1 m2]
   (validate-same-currency m1 m2)
   (->Money (+ (:amount m1) (:amount m2)) (:currency m1)))
  ([m1 m2 & monies]
   (reduce +$ m1 (conj monies m2))))

(defn *$
  [m n]
  (->Money (* n (:amount m)) (:currency m)))

(defn make-money
  ([] (make-money 0))
  ([amount] (make-money amount (:usd currencies)))
  ([amount currency] (->Money amount currency)))

(defmulti cost (fn [entity store] (class entity)))

(def a (chan 2))
(def b (chan 2))
(def c (chan 2))

(defn piping
  [ichan ochan]
  (pipe ichan ochan))

(defn splitting
  [f ichan tchan fchan]
  (split f ichan tchan fchan))

(defn logging
  [ichan ochan]
  (let [m (mult ichan)
        log (chan (dropping-buffer 100))]
    (tap m ochan)
    (tap m log)
    log))

(defrecord MailMachine
    [ichan ochan active])

(defn make-mail-machine
  [ichan ochan]
  (map->MailMachine {:ichan ichan
                     :ochan ochan
                     :active (atom false)}))

(defn start-mail-machine
  [{:keys [ichan ochan active] :as mail}]
  (reset! active true)
  (go-loop [request (<! ichan)]
      (>! ochan (inc request))
      (take! ochan (fn [b] (println (str "this is" b))))
      (when @active (recur (<! ichan))))
  mail)

(defn stop-mail-machine
  [{:keys [ochan active] :as mail}]
  (reset! active false)
  (async/close! ochan)
  mail)

(def macaca "sukajadi")
