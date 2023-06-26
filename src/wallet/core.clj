(ns wallet.core
  (:gen-class)
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found]]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :as mj]
            [wallet.configs :refer [api-configs]]
            [wallet.database :refer [create-account-if-not-exists
                                     create-tables-if-not-exists
                                     get-account-info
                                     get-movements-in-period
                                     is-balance-greater?
                                     transfer-funds
                                     get-movements-by-type]]))

(defn parse-date [date-str]
  (java.time.LocalDate/parse date-str))

(defn wrap-create-account-if-not-exists
  [handler]
  (fn [request]
    (let [headers (:headers request)
          user-id (get headers "x-user-id")
          name (get headers "x-name")]
      (when (not (nil? user-id))
        (create-account-if-not-exists user-id name))
      (handler request))))

(defn route-account-info
  [request]
  (let [headers (:headers request)
        user-id (get headers "x-user-id")]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string (get-account-info user-id))}))

(defn route-transfer-between-account
  [request]
  (let [headers (:headers request)
        user-id (get headers "x-user-id")
        body (-> request :body)
        account (get body "account")
        amount (get body "value")
        description (get body "description")]
    (if (and (not (nil? user-id))
             (not (nil? account))
             (not (nil? amount))
             (is-balance-greater? user-id amount))
      (do
        (transfer-funds user-id account amount description)
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:message "Transfer successful"})})
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:message "Insufficient balance or invalid account"})})))

(defn route-movements-in-period
  [request]
  (let [headers (:headers request)
        user-id (get headers "x-user-id")
        query-string (:query-string request)
        query-params (str/split query-string #"\&")
        query-map (into {}
                        (map #(str/split % #"\=") query-params))
        start-date-str (get query-map "start")
        end-date-str (get query-map "end")]
    (if (and (not (nil? start-date-str))
             (not (nil? end-date-str)))
      (let [start-date (parse-date start-date-str)
            end-date (parse-date end-date-str)
            movements (get-movements-in-period user-id start-date end-date)]
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body movements})
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:message "Invalid start date or end date"})})))

(defn route-movements-by-type
  [request]
  (let [headers (:headers request)
        user-id (get headers "x-user-id")
        query-string (:query-string request)
        query-params (str/split query-string #"\&")
        query-map (into {}
                        (map #(str/split % #"\=") query-params))
        movement-type (get query-map "type")]
    (if (not (nil? movement-type))
      (let [movements (get-movements-by-type user-id movement-type)]
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string movements)})
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:message "Invalid movement type"})})))

(defroutes routes
  (POST "/v1/transfer" [] (mj/wrap-json-body route-transfer-between-account))
  (GET "/v1/account" [] route-account-info)
  (GET "/v1/movements/period" [] route-movements-in-period)
  (GET "/v1/movements/type" [] route-movements-by-type)
  (not-found "<h1>Nothing here</h1>"))

(def api
  (-> routes
      (wrap-create-account-if-not-exists)))

(defn -main []
  (create-tables-if-not-exists)
  (println api-configs)
  (run-server api api-configs))
