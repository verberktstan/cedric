(ns cedric.core
  (:require [clojure.data :as data]))

;; CEDRIC - Clojure's Event Driven datapersistence Companion
;; Store items (maps) as rows in a EAV database. Backends implemented as in-memory db, csv file, and SQLite
;; Create, Read, Update & Delete/Destroy

(def zip-eav (partial zipmap [::entity ::attribute ::value ::deleted]))

(defn ->rows
  "Returns EAV-rows for the item to be saved. I'ts entity is based of the
  supplied entity-attribute. When the entity can't be found in the item, returns nil."
  [{:keys [entity-attribute deleted?]
    :or   {entity-attribute :id}} item]
  (when-let [entity (find item entity-attribute)]
    (->> (dissoc item entity-attribute)
         (map (comp zip-eav (juxt (constantly entity) key val (constantly (boolean deleted?)))))
         (map (juxt ::entity ::attribute ::value ::deleted)))))

;; TODO - Make this more readabe?
(defn ->map [{::keys [entity attribute value deleted]}]
  (if deleted
    {entity (with-meta {attribute value} {::deleted-attribute attribute})}
    {entity
     (-> {}
         (into [entity])
         (into [[attribute value]]))}))

(defn- merge-items [map-a map-b]
  (let [{::keys [deleted-attribute]} (meta map-b)]
    (cond-> map-a
      deleted-attribute       (dissoc deleted-attribute)
      (not deleted-attribute) (merge map-b))))

;; TODO - Add filtering (on entity?) of the rows before merging
(defn combine [rows]
  (transduce
    (comp
      (map zip-eav)
      (map ->map))
    (partial merge-with merge-items)
    {}
    rows))

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
