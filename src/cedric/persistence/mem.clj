(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]))

(defn- create [mem {:keys [entity-attribute]} & items]
  (assert (keyword? entity-attribute)) ;; TODO: DRY this!
  (let [created (apply c/create (::rows mem) entity-attribute items)]
    (-> mem
        (update ::rows concat (apply c/items->rows entity-attribute created))
        (assoc ::created created))))

(defn- destroy [{::keys [rows] :as mem} {:keys [entity-attribute]} & entity-vals]
  (assert (keyword? entity-attribute)) ;; TODO: DRY this!
  (let [db (c/merge-rows {:entity-attr? #{entity-attribute}
                          :entity-val? (set entity-vals)} rows)]
    (-> mem
        (update ::rows
                concat
                (apply c/destroyed-items->rows entity-attribute (vals db)))
        (assoc ::destroyed (vals db)))))

;; Assumes mem is an atom.
(defrecord Mem [mem]
  Persistence
  (create! [_ props items]
    (-> (apply swap! mem create props items) ::created))
  (query [_ props]
    (-> props (c/merge-rows (::rows @mem)) vals c/prune))
  (destroy! [_ props entity-vals]
    (-> (apply swap! mem destroy props entity-vals) ::destroyed)))
