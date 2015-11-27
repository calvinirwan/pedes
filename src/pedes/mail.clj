(ns pedes.mail
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan <! <!! >! >!!
                                        put! take!
                                        go go-loop
                                        split mult tap pipe
                                        dropping-buffer
                                        pipeline
                                        pipeline-blocking] :as async]
            [clojure.core.reducers :as r]))


;; (def a (chan (dropping-buffer 10)))

;; (def c1 (chan (dropping-buffer 10)))
;; (def c2 (chan (dropping-buffer 10)))
;; (def log-ch (chan 1024))
;; (defn ex-hand
;;   [ex]
;;   (>!! log-ch ex)
;;   :error)

;; (def x (let [xform (map (fn [x]
;;                           (assert (inc x))
;;                           (inc x)))
;;              ex ex-hand
;;              ch (chan 100 xform ex)]
;;          ch))
#_ (async/onto-chan ch [10 9 "8" 7 27] true)

(def xform (comp (map inc)))

(defn tiger
  [c1 c2 ex]
  (pipeline 4 c2 xform c1 true ex))

(defrecord Mail [in-ch out-ch log-ch]
  component/Lifecycle
  (start [{:keys [in-ch out-ch log-ch] :as this}]
    (let [ex (fn [ex]
               (>!! log-ch ex)
               :error)]
      (-> this
          (assoc :pipeline (pipeline 4 out-ch (map inc) in-ch true ex))))
    this)
  (stop [{:keys [in-ch out-ch log-ch] :as this}]
    (async/close! in-ch)
    (async/close! out-ch)
    (async/close! log-ch)
    (dissoc this :pipeline)))

(defn make-mail
  []
  (map->Mail {:in-ch (chan 10)
              :out-ch (chan 10)
              :log-ch (chan 100)}))

