(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]
            [clojure.data :as data]))

(defn- items-with-entities [db]
  (reduce-kv
    (fn [m entity item]
      (assoc m entity (merge item (into {} [entity]))))
    nil
    db))

(defn- upsert [rows {:keys [entity-attribute] :as props} item]
  (let [db                      (c/combine rows)
        entity                  (if-let [entity (find item entity-attribute)]
                                  entity
                                  (c/generate-entity props db))
        the-item                (merge item (into {} [entity]))
        [removed added overlap] (data/diff (get db entity) the-item)
        added-rows              (c/->rows props (into (or added {}) [entity]))
        removed-rows            (c/->rows (assoc props :deleted? true) (into (or removed {}) [entity]))]
    (with-meta
      (as-> rows r
        (reduce conj r removed-rows)
        (reduce conj r added-rows))
      {::item (merge-with merge added overlap)})))

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
