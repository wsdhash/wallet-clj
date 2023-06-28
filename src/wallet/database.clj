(ns wallet.database
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [wallet.configs :refer [db-configs]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn create-tables-if-not-exists []
  (jdbc/execute! db-configs
                 ["CREATE TABLE IF NOT EXISTS accounts (
       id VARCHAR(255) NOT NULL PRIMARY KEY,
       name VARCHAR(255) DEFAULT 'Ghost',
       balance DECIMAL(10, 2) DEFAULT 0,
       `limit` DECIMAL(10, 2) DEFAULT 10000
     );"])

  (jdbc/execute! db-configs
                 ["CREATE TABLE IF NOT EXISTS movements (
       txid VARCHAR(255) NOT NULL PRIMARY KEY,
       user VARCHAR(255) NOT NULL,
       address VARCHAR(255) NOT NULL,
       description VARCHAR(255),
       amount DECIMAL(10, 2) NOT NULL,
       `type` VARCHAR(255),
       `date` DATE,
       FOREIGN KEY (user) REFERENCES accounts(id)
     );"]))

(defn create-account-if-not-exists
  [id name]
  (jdbc/execute! db-configs
                 ["INSERT OR IGNORE INTO accounts (id, name, balance, `limit`)
      SELECT ?, ?, 5, 10000
      WHERE NOT EXISTS (
        SELECT 1 FROM accounts WHERE id = ?
      );"
                  id name]))

(defn get-account-info
  [user-id]
  (let [query "SELECT id, name, balance, `limit` FROM accounts WHERE id = ? LIMIT 1"
        result (jdbc/query db-configs [query user-id])]
    (if (seq result)
      (json/generate-string (first result))
      nil)))

(defn is-balance-greater?
  [user-id value]
  (let [query "SELECT balance FROM accounts WHERE id = ? LIMIT 1"
        result (jdbc/query db-configs [query user-id])]
    (when-let [account (first result)]
      (> (:balance account) value))))

(defn transfer-funds
  [from-user-id to-user-id amount description]
  (let [from-account-query "UPDATE accounts SET balance = balance - ? WHERE id = ?"
        to-account-query "UPDATE accounts SET balance = balance + ? WHERE id = ?"
        movement-query "INSERT INTO movements (txid, user, address, description, amount, `type`, `date`)
                        VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE)"]
    (jdbc/execute! db-configs [from-account-query amount from-user-id])
    (jdbc/execute! db-configs [to-account-query amount to-user-id])
    (jdbc/execute! db-configs [movement-query (uuid) from-user-id to-user-id description amount "DEBIT"])
    (jdbc/execute! db-configs [movement-query (uuid) from-user-id to-user-id description amount "CREDIT"])))

(defn get-movements-in-period
  [user-id start end]
  (let [query "SELECT txid, user, address, description, amount, `type`, `date`
               FROM movements
               WHERE user = ? AND `date` BETWEEN ? AND ?"
        result (jdbc/query db-configs [query user-id start end])]
    (json/generate-string result)))

(defn get-movements-by-type
  [user-id movement-type]
  (jdbc/query db-configs
              ["SELECT * FROM movements WHERE user = ? AND type = ?"
               user-id movement-type]))

(defn get-movements-by-type-and-date
  [user-id movement-type start end]
  (let [query "SELECT txid, user, address, description, amount, `type`, `date`
               FROM movements
               WHERE user = ? AND `type` = ? AND `date` BETWEEN ? AND ?"
        result (jdbc/query db-configs [query user-id movement-type start end])]
    (json/generate-string result)))
