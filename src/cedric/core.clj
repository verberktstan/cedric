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
         (map (comp zip-eav (juxt (constantly entity) key val (constantly deleted?))))
         (map (juxt ::entity ::attribute ::value ::deleted)))))

(defn entity->map
  "Returns a map with the entity as part of it.
  `(entity->map {:a :b} [:id 0]) => {:a :b :id 0}`"
  ([entity] (entity->map nil entity))
  ([m entity]
   (into (or m {}) [entity])))

;; TODO - Make this more readable?
;; TODO - Make deleted and destroyed work the same way, possibly without metadata?
(defn ->map [{::keys [entity attribute value deleted]}]
  (cond
    (#{:destroy!} deleted)
    (with-meta {} {:destroyed-entity entity})
    deleted
    {entity (with-meta {attribute value} {::deleted-attribute attribute})}
    :else
    {entity
     (-> {}
         (entity->map entity)
         (into [[attribute value]]))}))

(defn- merge-item [map-a map-b]
  (let [{::keys [deleted-attribute]} (meta map-b)]
    (cond-> map-a
      deleted-attribute       (dissoc deleted-attribute)
      (not deleted-attribute) (merge map-b))))

(defn merge-items [& [item-a item-b]]
  (let [{:keys [destroyed-entity]} (meta item-b)]
    (cond-> (merge-with merge-item item-a item-b)
      destroyed-entity (dissoc destroyed-entity))))

(defn combine
  ([row] (combine nil row))
  ([{:keys [entity-pred]
     :or   {entity-pred identity}
     :as   props} rows]
   (transduce
     (comp
       (map zip-eav)
       (filter (comp entity-pred ::entity))
       ;; Catch :destroy in ->map
       (map ->map))
     ;; Instead of merge-with consume :destroy there.
     merge-items 
     ;; (partial merge-with merge-item)
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
