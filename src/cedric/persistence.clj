(ns cedric.persistence)

(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (create! [this props items] "Creates a new entity, persists and returns the items.")
  (rows [this] "Returns the raw rows in the db.")
  (query [this props] "Returns items from the database."))
