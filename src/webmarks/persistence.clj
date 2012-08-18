(ns webmarks.persistence)

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