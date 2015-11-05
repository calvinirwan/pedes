(ns pedes.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [ring.util.response :as ring-resp]
            [ring.handler.dump :refer [handle-dump]]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.test :as ptest]
            [io.pedestal.http.csrf :as csrf] 
            [clj-http.client :as cl]
            [clj-http.cookies :refer [cookie-store]]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response (str "Hello berok!" (:params request))))

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

(defroutes routes
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/" ^:interceptors [(csrf/anti-forgery)
                         (body-params/body-params)
                         (middlewares/params)]
     {:get home-page}
     ;^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/about" {:get about-page}]
     ["/req" {:any nuthin}]
     ["/ctx" {:any sumthin}]]]])

;; Consumed by pedes.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/enable-csrf {}
              ::bootstrap/enable-session {}
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 3000})

(def sagat
  (::bootstrap/service-fn (bootstrap/create-servlet service)))

(def b
  "{\"json\": \"input\"}"
  #_"{:ninja \"tiger\"}")

(def h
  {"Content-Type" "application/json"
   "Accept" "application/json"})

(defn tiger
  []
  (:body (ptest/response-for sagat :post "/req" :body b :headers h)))

(defn macaca
  []
  (let [kue (cookie-store)
        r1 (cl/get "http://localhost:3000/req"
                   {:throw-exceptions false
                    :cookie-store kue})
        token-regex (str "name=\"__anti-forgery-token\" "
                           "type=\"hidden\" value=\"(.+?)\"")
        token (-> token-regex
                  re-pattern
                  (re-find (:body r1))
                  second)
        r2 (cl/post
              "http://localhost:3000/req"
              {:headers {"X-CSRF-Token" token}
               :form-params {:foo "bar"}
               :throw-exceptions false
               :cookie-store kue})]
    r2))
