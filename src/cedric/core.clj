(ns cedric.core)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CEDRIC - the Cedric Event DRIven datapersistence Companion
;; Store associatve data (maps) as rows in a EAV database.
;; Backends implemented as in-memory db (TODO - csv, edn and SQLite implementations)

(def prune (comp seq (partial keep identity)))

(defn find-entity
  "Finds entity from the item, and checks if it is a valid map-entry. Assumes
  item is a map and entity-attribute is a key that's present in map item."
  [entity-attribute item]
  {:post [(map-entry? %)]}
  (find item entity-attribute))

#_(defn destroyed-items->rows
  "Returns rows for items to be destroyed."
  [entity-attribute & items]
  (letfn [(rowify [item]
            [[(find-entity entity-attribute item) nil nil :destroyed]])]
    (mapcat rowify items)))

(defn- rowify "Returns a function that returns a row for a map-entry."
  [entity]
  (juxt (constantly entity) key val))

(defn items->rows [entity-attribute & items]
  (letfn [(->rows [item]
            (map
             (rowify (find-entity entity-attribute item))
             (dissoc item entity-attribute)))]
    (mapcat ->rows items)))

#_(defn- merge-db-item [db-item {::keys [destroyed] :as item}]
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
      (partial merge-with merge #_-db-item)
      rows))))

(defn create [rows entity-attribute & items]
  (when (seq items)
    (let [db (merge-rows {:entity-attr? #{entity-attribute}} rows)
          next-entities (->> (range)
                           (map (juxt (constantly entity-attribute) identity))
                           (remove (or db {})))]
      (map #(into %1 [%2]) items next-entities))))

#_(defn destroy [rows entity-attribute & entity-vals]
  (when (seq entity-vals)
    (let [entities (set (map vector (repeat entity-attribute) (seq entity-vals)))
          db (merge-rows {:entity? entities} rows)]
      (map #(with-meta % {::destroyed-entity %}) (keys db)))))
