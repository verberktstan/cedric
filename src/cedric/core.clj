(ns cedric.core
  (:require [clojure.data :as data]))

;; CEDRIC - Clojure's Event Driven datapersistence Companion
;; Store items (maps) as rows in a EAV database. Backends implemented as in-memory db, csv file, and SQLite
;; Create, Read, Update & Delete/Destroy

(def zip-eav (partial zipmap [::entity ::attribute ::value]))

(defn ->rows
  "Returns EAV-rows for the item to be saved. I'ts entity is based of the
  supplied entity-attribute. When the entity can't be found in the item, returns nil."
  [{:keys [entity-attribute]
    :or   {entity-attribute :id}} item]
  (when-let [entity (find item entity-attribute)]
    (->> (dissoc item entity-attribute)
         (map (comp zip-eav (juxt (constantly entity) key val)))
         (map (juxt ::entity ::attribute ::value)))))

;; TODO - Make this more readabe?
(defn ->map [{::keys [entity attribute value]}]
  {entity
   (-> {}
       (into [entity])
       (into [[attribute value]]))})

;; TODO - Add filtering (on entity?) of the rows before merging
(defn combine [rows]
  (transduce
    (comp
      (map zip-eav)
      (map ->map))
    (partial merge-with merge)
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
