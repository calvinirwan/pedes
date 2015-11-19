(ns pedes.interceptor
  (:require [io.pedestal.interceptor :refer [interceptor]]
            [ring.handler.dump :refer [handle-dump]]
            [clj-http.cookies :refer [cookie-store]]
            [ring.util.response :as ring-resp]
            [ring.handler.dump :refer [handle-dump]]
            [io.pedestal.http.route :as route]))

(def nuthin
  (interceptor
   {:name ::nuthin
    :enter (fn [ctx]
             (assoc ctx :response (handle-dump ctx)))}))

(def sumthin
  (interceptor
   {:name ::sumthin
    :enter (fn [ctx] (assoc ctx :response (handle-dump ctx)))}))

(def interctx
  (interceptor
   {:name ::intereq
    :enter (fn [ctx]
             (assoc ctx :response
                    (ring-resp/response (str ctx))))}))
(def intereq
  (interceptor
   {:name ::intereq
    :enter (fn [ctx]
             (assoc ctx :response
                    (ring-resp/response (str (:request ctx)))))}))

(defn mail-interceptor
  [mail]
  (interceptor
   {:name ::mail-interceptor
    :enter 
    (fn [ctx]
      (assoc ctx :mail mail))}))
