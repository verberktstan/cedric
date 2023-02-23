(ns cedric.persistence)

;; TODO - Add CSV implementation of Persistence
;; TODO - Add EDN implementation of Persistence
;; TODO - Add SQLite implementation of Persistence
(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (read-all [this] "Returns all items in the database.")
  (upsert! [this props item] "Upsert changes to the item. Returns the item.")
  (destroy! [this props item]))
