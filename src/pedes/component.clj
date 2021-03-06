(ns pedes.component
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan <! <!! >! >!!
                                        put! take!
                                        go go-loop
                                        split mult tap pipe
                                        dropping-buffer] :as async]
            [clojure.core.reducers :as r]))

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

(def xform (comp (map inc) (map inc) (map dec)))

#_(time (transduce xform + (range 10000000)))

#_(time (r/reduce + (r/map inc (r/map inc (r/map dec (range 10000000))))))

#_(time (reduce + (map inc (map inc (map dec (range 10000000))))))

(defn a [x]
  (println "A:" x)
  x)

(defn b [x]
  (println "B:" x)
  x)

(defn c [x]
  (println "C:" x)
  x)

(def printer (agent nil))

(defn m [x]
  (send printer (fn [_] (println "A:" x)))
  (Thread/sleep 500)
  x)

(defn n [x]
  (send printer (fn [_] (println "B:" x)))
  (Thread/sleep 1000)
  x)

(defn o [x]
  (send printer (fn [_] (println "C:" x)))
  (Thread/sleep 2000)
  x)

(defn pipeline []
  (let [bound 10000
        m-ch (chan bound)
        n-ch (chan bound)
        o-ch (chan bound)]
    (go (while true (put! n-ch (m (<! m-ch)))))
    (go (while true (put! o-ch (n (<! n-ch)))))
    (go (while true (o (<! o-ch))))
    m-ch))

(defn demo []
  (let [head-ch (pipeline)]
    (doseq [k (range 10)]
      (go (put! head-ch k)))))

