(ns cedric.persistence)

;; TODO - Add CSV implementation of Persistence
;; TODO - Add EDN implementation of Persistence
;; TODO - Add SQLite implementation of Persistence
(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (query [this props] "Queries items from the database.")
  (upsert! [this props item] "Upsert changes to the item. Returns the item.")
  (destroy! [this props item]))
