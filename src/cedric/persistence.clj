(ns cedric.persistence)

;; TODO - Implement destroy!
;; TODO - Add CSV implementation of Persistence
;; TODO - Add SQLite implementation of Persistence
(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (create! [this props item] "Creates and saves item as EAV row. Returns the item.")
  (read-all [this] "Returns all items in the database.")
  (update! [this props item] "Updates changes to the item. Returns the item."))
