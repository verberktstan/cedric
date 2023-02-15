(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]
            [clojure.data :as data]))

(defn- items-with-entities [db]
  (reduce-kv (fn [m entity item]
               (assoc m entity (merge item (into {} [entity])))) nil db))

(defn- upsert [rows {:keys [entity-attribute] :as props} item]
  (let [db                (c/combine rows)
        entity            (if-let [entity (find item entity-attribute)]
                            entity
                            ((c/make-entity-generator db) entity-attribute)) ;; TODO - Refactor this
        item-rows         (c/->rows props (merge item (into {} [entity])))
        [_ added overlap] (data/diff db (->> item-rows (map c/zip-eav) c/->map))] ;; TODO - Refactor zip-eav ->map
    (with-meta
      (reduce conj rows (mapcat (partial c/->rows props) (vals (items-with-entities added))))
      {::item (-> (merge-with merge added overlap) (get entity))})))

(defrecord Mem [mem]
  Persistence
  (create! [this {:keys [entity-attribute] :as props} item]
    (-> mem
        (swap! upsert props (dissoc item entity-attribute))
        meta
        ::item))
  (read-all [this] (c/combine @mem))
  (update! [this props item]
    (-> mem
        (swap! upsert props item)
        meta
        ::item)))
