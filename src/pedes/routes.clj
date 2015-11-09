(ns pedes.routes
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.test :as ptest]
            [io.pedestal.http.csrf :as csrf] 
            [clj-http.client :as cl]
            [clj-http.cookies :refer [cookie-store]]
            [pedes.interceptor :refer [nuthin sumthin csrf-hack
                                       home-page about-page]]))

(defroutes routes
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/" ^:interceptors [(csrf/anti-forgery)
                         (body-params/body-params)
                         (middlewares/params)
                         ]
     {:get home-page}
     ;^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/about" {:get about-page}]
     ["/req" {:any nuthin}]
     ["/ctx" {:any sumthin}]]]])
