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

;; TODO - Make the entity-value generator (range in this case) a input parameter.
(defn make-entity-generator [db]
  (fn gen-entity* [entity-attribute]
    (first
      (into
        []
        (comp
          (map (juxt (constantly entity-attribute) identity))
          (drop-while db)
          (take 1))
        (range)))))
