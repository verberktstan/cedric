(ns cedric.core
  (:require [cedric.eav-map :as eav-map]
            [clojure.data :as data]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CEDRIC - Companion for Event DRIven datapersistence in Clojure
;; Store associatve items (maps, vectors, records) as rows in a EAV database.
;; Backends implemented as in-memory db, (TODO - csv, edn and SQLite)
;; Upsert (create, update & delete), Read & Destroy functionality

(defn ->rows
  "Returns EAV-rows for the item to be saved. I'ts entity is based of the
  supplied entity-attribute. When the entity can't be found in the item, returns nil."
  [{:keys [entity-attribute find-entity deleted?]} item]
  (when-let [entity (or (find item entity-attribute)
                        (when find-entity (find-entity item)))]
    (let [delete (when deleted? :delete!)]
      (->> (dissoc item entity-attribute)
           (map (eav-map/make entity delete))
           (map eav-map/->row)))))

;; TODO - Refactor this into its own item ns
(defn- merge-item [map-a map-b]
  (let [{::eav-map/keys [deleted-attribute]} (meta map-b)]
    (cond-> map-a
      deleted-attribute       (dissoc deleted-attribute)
      (not deleted-attribute) (merge map-b))))

(defn- merge-items [& [item-a item-b]]
  (let [{::eav-map/keys [destroyed-entity]} (meta item-b)]
    (cond-> (merge-with merge-item item-a item-b)
      destroyed-entity (dissoc destroyed-entity))))

(defn- entity-filter
  "Returns a transducer."
  [{:keys [ea? ev? e?]}]
  (filter
    (if (or ea? ev? e?)
      (fn entity-pred [{::eav-map/keys [entity]}]
        (every?
          (fn [predicate] (predicate entity))
          (keep
            identity
            [e?
             (when ea? (comp ea? first))
             (when ev? (comp ev? second))])))
      identity)))

(defn combine
  ([row] (combine nil row))
  ([props rows]
   (transduce
     (comp
       (map eav-map/zip)
       (entity-filter props)
       (map eav-map/->map))
     merge-items
     {}
     rows)))

(defn- take-next [entity-attribute db]
  (comp
    (map (juxt (constantly entity-attribute) identity))
    (drop-while db)
    (take 1)))

(defn generate-entity
  [{:keys [entity-attribute entity-value-generator]
    :or   {entity-value-generator range}}
   db]
  (first
    (into
      []
      (take-next entity-attribute db)
      (entity-value-generator))))
