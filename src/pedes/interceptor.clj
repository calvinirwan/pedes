(ns pedes.interceptor
  (:require [io.pedestal.interceptor :refer [interceptor]]
            [ring.handler.dump :refer [handle-dump]]
            [clj-http.cookies :refer [cookie-store]]
            [ring.util.response :as ring-resp]
            [ring.handler.dump :refer [handle-dump]]
            [io.pedestal.http.route :as route]))

(def nuthin
  (interceptor
   {:name :nuthin
    :enter (fn [ctx]
             (assoc ctx :response
                    (ring-resp/response (:request ctx))))}))

(def sumthin
  (interceptor
   {:name :sumthin
    :enter (fn [ctx] (assoc ctx :response (ring-resp/response ctx)))}))

(def csrf-hack
  (interceptor
   {:name ::csrf-hack
    :enter (fn [ctx]
             (let [context (-> ctx
                               (assoc-in [:request :headers "x-csrf-token"] "anjing" )
                               (assoc-in [:request :session "__anti-forgery-token"] "anjing" ))]
               context))}))


(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response (str "Hello berok!" (:params request))))
