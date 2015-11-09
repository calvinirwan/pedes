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


(def a (chan (dropping-buffer 10)))

(def c1 (chan (dropping-buffer 10)))
(def c2 (chan (dropping-buffer 10)))

(def xform (comp (map #(try %
                            (catch Exception e (str "caught exception: ini salah bero" (.getMessage e))))) (map inc)))

(defn tiger
  [c1 c2]
  (pipeline 4 c2 xform c1))
