(ns cedric.core
  (:require [clojure.data :as data]))

;; CEDRIC - your Companion for Event DRIven data persistence in Clojure
;; Store items (maps) as rows in a EAV database. Backends implemented as in-memory db, csv file, and SQLite
;; Create, Read, Update & Delete/Destroy

(def zip-eav (partial zipmap [::entity ::attribute ::value]))

(defn ->rows
  "Returns EAV-rows for the item to be saved. I'ts entity is based of the
  supplied entity-attribute. If that can't be found in the item, gen-entity is
  evaluated to generate a new entity."
  [{:keys [entity-attribute gen-entity]
    :or   {entity-attribute :id
           gen-entity       (constantly nil)}}
   item]
  (let [entity (or (find item entity-attribute)
                   (gen-entity entity-attribute))]
    (assert (some? entity))
    (->> (dissoc item entity-attribute)
         (map (comp zip-eav (juxt (constantly entity) key val)))
         (map (juxt ::entity ::attribute ::value)))))

(defn ->map [{::keys [entity attribute value] :as props}]
  {entity
   (-> {}
       (into [entity])
       (into [[attribute value]]))})

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
