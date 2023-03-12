(ns cedric.persistence)

(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (create! [this props items] "Creates a new entity, persists and returns the items.")
  (query [this props] "Returns items from the database.")
  (destroy! [this props entity-vals] "Destroyes items, and returns the destroyed items."))
