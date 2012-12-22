(ns webmarks.persistence
  (require [clojure.java.jdbc :as jdbc])
  (import java.util.Date
          java.sql.Timestamp))

(defprotocol PersistentContainer
  "A protocol to persist data structures into some container."
  (save-data [this data] "Persist data into the container")
  (load-data [this] "Load data from the container"))

(deftype ClojureFile [filename]
  PersistentContainer
  (save-data [this data]
    (spit (.filename this) (pr-str data)))
  (load-data [this]
    (read-string (slurp (.filename this)))))

;; Storage on PostgreSQL
;; =======================

;; TODO Rotate

(defn create-storage-table [db-spec]
  (jdbc/with-connection db-spec
    (jdbc/create-table :storage
                       [:timestamp "timestamp primary key"]
                       [:edn "text"])))

(defn fetch-results [db-spec query]
  (jdbc/with-connection db-spec
    (jdbc/with-query-results res query
      (doall res))))

(defn insert-record [db-spec table record]
  (jdbc/with-connection db-spec
    (jdbc/insert-record table record)))

(defmacro run-transaction [db-spec & forms]
  `(jdbc/with-connection ~db-spec
     (jdbc/transaction ~@forms)))

(defn- rotate-records [db-spec max]
  (let [timestamps (fetch-results db-spec
                                  ["select timestamp from storage order by timestamp asc"])]
    (if (= max (count timestamps))
      (run-transaction db-spec
                       (jdbc/delete-rows :storage
                                         ["timestamp = ?" (:timestamp (first timestamps))])))))

(defmacro ensure-table [update-fn create-table-fn]
  `(try ~update-fn
        (catch org.postgresql.util.PSQLException e#
          (do ~create-table-fn
              ~update-fn))))

(deftype PostgresDatabase [db-spec max-records]
  PersistentContainer
  (save-data [this data]
    (let [record {:timestamp (Timestamp. (.getTime (Date.)))
                  :edn (pr-str data)}]
      (ensure-table
       (rotate-records db-spec max-records)
       (create-storage-table db-spec))
      (insert-record db-spec :storage record)))
  (load-data [this]
    (first
     (fetch-results db-spec
                    ["SELECT edn FROM storage ORDER BY timestamp DESC LIMIT 1"]))))
