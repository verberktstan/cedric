(ns cedric.persistence
  (:require [cedric.core :as c]
            [cedric.entity :as entity]
            [clojure.data :as data]))

(defn prepare-upsert [{:keys [entity-attribute get-rows] :as props} item]
  (let [found-entity            (find item entity-attribute)
        combine-props           (if found-entity
                                  {:e? #{found-entity}}
                                  {:ea? #{entity-attribute}})
        db                      (c/combine combine-props (get-rows))
        entity                  (or found-entity (c/generate-entity props db))
        complete-item           (merge item (entity/->map entity))
        [removed added overlap] (data/diff (get db entity) complete-item)
        updated                 (when added (comp added key))
        deleted                 (cond->> removed
                                  added (into {} (remove (comp added key))))
        deleted-props           (assoc props :deleted? true)]
    {:deleted      deleted
     :added        added
     :overlap      overlap
     :added-rows   (c/->rows props (entity/->map added entity))
     :deleted-rows (c/->rows deleted-props (entity/->map deleted entity))}))

;; TODO - Add CSV implementation of Persistence
;; TODO - Add EDN implementation of Persistence
;; TODO - Add SQLite implementation of Persistence
(defprotocol Persistence
  "A simple protocol to persist EAV rows"
  (query [this props] "Queries items from the database.")
  (upsert! [this props item] "Upsert changes to the item. Returns the item.")
  (destroy! [this props item]))
