(ns wallet.configs
  (:require [dotenv :refer [env]]))

(def api-configs
  {:host (-> env :API_HOST (or "0.0.0.0"))
   :port (-> env :API_PORT (or "8080") (Integer/parseInt))})

(def db-configs
  {:dbtype   (or (env :DB_TYPE) "sqlite")
   :host     (or (env :DB_HOST) "127.0.0.1")
   :port     (Integer. (or (env :DB_PORT) "5432"))
   :dbname   (or (env :DB_NAME) "database.sqlite")
   :user     (env :DB_USER)
   :password (env :DB_PASS)})
