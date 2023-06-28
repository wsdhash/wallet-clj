(ns wallet.core
  (:gen-class)
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found]]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :as mj]
            [wallet.configs :refer [api-configs]]
            [wallet.database :refer [create-account-if-not-exists
                                     create-tables-if-not-exists
                                     get-account-info
                                     get-movements-by-type
                                     get-movements-by-type-and-date
                                     get-movements-in-period
                                     is-balance-greater?
                                     transfer-funds]]))

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
     :body (get-account-info user-id)}))

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
             (not (neg? amount))
             (is-balance-greater? user-id amount))
      (do
        (transfer-funds user-id account amount description)
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:message "Transfer successful"})})
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:message "Insufficient balance or invalid account or negative amount"})})))

(defn route-movements
  [request]
  (let [headers (:headers request)
        user-id (get headers "x-user-id")
        query-string (:query-string request)
        query-params (str/split query-string #"\&")
        query-map (into {}
                        (map #(str/split % #"\=") query-params))
        start-date-str (get query-map "start")
        end-date-str (get query-map "end")
        movement-type (get query-map "type")]
    (cond
      (and (not (nil? start-date-str))
           (not (nil? end-date-str))
           (not (nil? movement-type)))
      (let [start-date (parse-date start-date-str)
            end-date (parse-date end-date-str)
            movements (get-movements-by-type-and-date user-id movement-type start-date end-date)]
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body movements})

      (not (nil? movement-type))
      (let [movements (get-movements-by-type user-id movement-type)]
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string movements)})

      (and (not (nil? start-date-str))
           (not (nil? end-date-str)))
      (let [start-date (parse-date start-date-str)
            end-date (parse-date end-date-str)
            movements (get-movements-in-period user-id start-date end-date)]
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body movements})

      :else
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string
              {:message "Invalid query parameters"})})))

(defroutes routes
  (POST "/v1/transfer" [] (mj/wrap-json-body route-transfer-between-account))
  (GET "/v1/account" [] route-account-info)
  (GET "/v1/movements" [] route-movements)
  (not-found "<h1>Nothing here</h1>"))

(def api
  (-> routes
      (wrap-create-account-if-not-exists)))

(defn -main []
  (create-tables-if-not-exists)
  (println api-configs)
  (run-server api api-configs))
