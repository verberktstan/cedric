(ns cedric.persistence)

(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (create! [this props items] "Creates a new entity, persists and returns the items.")
  (rows [this] "Returns the raw rows in the db.")
  (query [this props] "Returns non-destroyed items from the database.")
  (destroy! [this props items] "Returns the entities of the destroyed items, and marks those items as destroyed."))
