(ns cedric.core)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CEDRIC - the Cedric Event DRIven datapersistence Companion
;; Store associatve data (maps) as rows in a EAV database.
;; Backends implemented as in-memory db (TODO - csv, edn and SQLite implementations)

(def prune (comp seq (partial keep identity)))

(defn- ->row [entity]
  (juxt (constantly entity) key val))

(defn destroyed-items->rows [entity-attribute & items]
  (letfn [(->rows [item]
            (let [entity (find item entity-attribute)]
              (assert entity) ;; TODO - DRY this!
              [[entity nil nil :destroyed]]))]
    (mapcat ->rows items)))

(defn items->rows [entity-attribute & items]
  (letfn [(->rows [item]
            (let [entity (find item entity-attribute)]
              (assert entity) ;; TODO - DRY this!
              (map (->row entity) (dissoc item entity-attribute))))]
    (mapcat ->rows items)))

(defn- merge-db-item [db-item {::keys [destroyed] :as item}]
  (when-not destroyed (merge db-item item)))

(defn merge-rows
  ([rows] (merge-rows nil rows))
  ([{:keys [entity? entity-attr? entity-val?]
     :or {entity? identity entity-attr? identity entity-val? identity}} rows]
   (letfn [(row->eav [row]
             (zipmap [::entity ::attribute ::value ::destroyed] row))
           (eav->map [{::keys [entity attribute value destroyed]}]
             {entity
              (cond-> {}
                attribute (assoc attribute value)
                :always (into [entity])
                destroyed (assoc ::destroyed entity))})]
     (transduce
      (comp (map row->eav)
            (filter (comp entity? ::entity))
            (filter (comp entity-attr? first ::entity))
            (filter (comp entity-val? second ::entity))
            (map eav->map))
      (partial merge-with merge-db-item)
      rows))))

(defn create [rows entity-attribute & items]
  (when (seq items)
    (let [db (merge-rows {:entity-attr? #{entity-attribute}} rows)
          next-entities (->> (range)
                           (map (juxt (constantly entity-attribute) identity))
                           (remove (or db {})))]
      (map #(into %1 [%2]) items next-entities))))

(defn destroy [rows entity-attribute & entity-vals]
  (when (seq entity-vals)
    (let [entities (set (map vector (repeat entity-attribute) (seq entity-vals)))
          db (merge-rows {:entity? entities} rows)]
      (map #(with-meta % {::destroyed-entity %}) (keys db)))))
