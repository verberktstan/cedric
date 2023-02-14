(ns cedric.persistence)

(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (create! [this props item] "Creates and saves item as EAV row. Returns the item.")
  (read-all [this] "Returns all items in the database."))
