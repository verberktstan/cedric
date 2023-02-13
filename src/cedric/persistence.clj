(ns cedric.persistence
  (:require [cedric.core :as c]))

(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (create! [this props items] "Creates a new entity for each of the items, persist and return the items."))
