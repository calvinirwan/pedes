(ns pedes.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as bootstrap]
            [pedes.service :as service]
            [io.pedestal.test :as ptest]))

#_(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

#_(def sagat
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(def b
  "{\"json\": \"input\"}"
  #_"{:ninja \"tiger\"}")

(def h
  {"Content-Type" "application/json"
   "Accept" "application/json"})

#_(defn tiger
  []
  (:body (ptest/response-for sagat :post "/req" :body b :headers h)))

#_(deftest home-page-test
  (is (=
       (:body (response-for service :get "/"))
       "Hello World!"))
  (is (=
       (:headers (response-for service :get "/"))
       {"Content-Type" "text/html;charset=UTF-8"
        "Strict-Transport-Security" "max-age=31536000; includeSubdomains"
        "X-Frame-Options" "DENY"
        "X-Content-Type-Options" "nosniff"
        "X-XSS-Protection" "1; mode=block"})))


#_(deftest about-page-test
  (is (.contains
       (:body (response-for service :get "/about"))
       "Clojure 1.6"))
  (is (=
       (:headers (response-for service :get "/about"))
       {"Content-Type" "text/html;charset=UTF-8"
        "Strict-Transport-Security" "max-age=31536000; includeSubdomains"
        "X-Frame-Options" "DENY"
        "X-Content-Type-Options" "nosniff"
        "X-XSS-Protection" "1; mode=block"})))

