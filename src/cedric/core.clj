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
