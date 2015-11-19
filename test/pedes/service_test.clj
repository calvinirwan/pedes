(ns pedes.service-test
  (:require [kerodon.core :refer :all]
            [kerodon.test :refer :all][clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.log :as log]
            [io.pedestal.http :as bootstrap]
            [pedes.system :as system]
            [io.pedestal.test :as ptest]
            [clojure.edn :as edn]))

(deftest system-check
  (let [dev system/dev-system]
    (is (= 4 (count (keys dev))))))


(deftest tiger-knee
  (is (= 2 2)))

(deftest tiger-uppercut
  (is 2))

(deftest tiger-undercut
  (is (= 2 2)))

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

(defn system->ring-handler [system]
  (let [;; system (-> system
        ;;            surface-csrf-token
        ;;            (cond->
        ;;             (.startsWith ^String (get-in system [:db :db-connect-string] "")
        ;;                          "jdbc:derby:memory:")
        ;;             (update-in [:db :db-connect-string]
        ;;                        #(str "jdbc:derby:memory:" (gensym "pertestdb") (subs % (count "jdbc:derby:memory:")))))
        ;;            (dissoc :jetty))
        service-fn (-> system
                       :web-server
                       :server
                       :io.pedestal.http/service-fn)]
    (fn [req]
      (let [method (:request-method req)
            uri (:uri req)
            ;; pedestal blows up when it can't figure out scheme /
            ;; host etc, which is missing as jetty is not running
            uri (if (not (.startsWith ^String uri "http"))
                  (str "http://testhost" uri)
                  uri)
            uri (if-let [qs (:query-string req)]
                  (str uri "?" qs)
                  uri)
            options (cond-> []
                            (:body req)
                            (into [:body (slurp (:body req))])
                            (:headers req)
                            (into [:headers (zipmap (map #(get {"content-type" "Content-Type"
                                                                "accept" "Accept"} % %) (keys (:headers req)))
                                                    (vals (:headers req)))]))

            response (apply ptest/response-for service-fn method uri options)]
        (cond-> response
                (.startsWith ^String (get-in response [:headers "Content-Type"] "") "application/edn")
                (assoc :edn (try (edn/read-string (get response :body))
                                 (catch Exception e
                                   (log/info :unreadable (get response :body))
                                   (throw e)))))))))

(defn kero
  []
  (let [dev system/dev-system
        hand (system->ring-handler dev)]
    (-> (session hand)
        (visit "/")
        (has (text? "hello")))))
