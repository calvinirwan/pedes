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
            [ring.util.response :as ring-resp]
            [clj-http.client :as cl]
            [clj-http.cookies :refer [cookie-store]]
            [pedes.interceptor :refer [nuthin sumthin
                                       intereq interctx
                                       mail-interceptor]]))

(defn routes
  [mail]
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/"
     ^:interceptors [(csrf/anti-forgery)
                     (mail-interceptor mail)
                     (body-params/body-params)
                     (middlewares/params)]
     {:any nuthin}
     ;["/req" {:any intereq}]            
     ["/ctx" {:any interctx}]]]])

#_(defn hello-world [req] {:status 200 :body "Hello World!"})
#_(defn macaca [req]
  {:status 200 :body "ratata"})

#_(def route-table
  (expand-routes '[[["/" ^:interceptors
                     [(csrf/anti-forgery)
                      (body-params/body-params)
                      (middlewares/params)
                      nuthin]
                     {:any macaca}
                     ["/hello-world" {:any hello-world}]]]]))

(defn make-routes
  [mail]
  (expand-routes (routes mail)))

(defrecord RoutesMap [routes mail]
  component/Lifecycle
  (start [this]
    (let [routes (make-routes mail)]
      (assoc this :routes routes)))
  (stop [component]
    (dissoc component :routes)))

(defn make-routes-map
  []
  (map->RoutesMap {}))
