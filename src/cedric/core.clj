(ns cedric.core)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CEDRIC - Companion for Event DRIven datapersistence in Clojure
;; Store associatve data (maps) as rows in a EAV database.
;; Backends implemented as in-memory db (TODO - csv, edn and SQLite implementations)

(defn items->rows [entity-attribute & items]
  (letfn [(->rows [item]
            (let [entity (find item entity-attribute)]
              (assert entity)
              (map
               (juxt (constantly entity) key val)
               (dissoc item entity-attribute))))]
    (mapcat ->rows items)))

(defn merge-rows [rows]
  (letfn [(row->eav [row]
            (zipmap [::entity ::attribute ::value] row))
          (eav->map [{::keys [entity attribute value]}]
            {entity (into {attribute value} [entity])})]
    (transduce
     (comp (map row->eav) (map eav->map))
     (partial merge-with merge)
     rows)))

(defn create [rows entity-attribute & items]
  (let [db (merge-rows rows)
        next-entities (->> (range)
                           (map (juxt (constantly entity-attribute) identity))
                           (remove (or db {})))]
    (map #(into %1 [%2]) items next-entities)))
