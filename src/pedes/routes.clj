(ns pedes.routes
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes expand-routes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.test :as ptest]
            [io.pedestal.http.csrf :as csrf] 
            [clj-http.client :as cl]
            [clj-http.cookies :refer [cookie-store]]
            [pedes.interceptor :refer [nuthin sumthin csrf-hack
                                       home-page about-page]]))

(defn routes
  []
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/" ^:interceptors [(csrf/anti-forgery)
                         (body-params/body-params)
                         (middlewares/params)]
     ["/req" {:any nuthin}]
     ["/ctx" {:any sumthin}]]]])

(defn make-routes
  []
  (expand-routes (routes)))

(defrecord RoutesMap [routes mail]
  component/Lifecycle
  (start [this]
    (let [routes (make-routes)]
      (assoc this :routes routes)))
  (stop [component]
    (dissoc component :routes)))

(defn make-routes-map
  []
  (map->RoutesMap {}))
