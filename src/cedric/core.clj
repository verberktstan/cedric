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

(defn- rowify* "Returns a function that returns a row for a map-entry."
  [entity tx]
  (assert entity)
  (assert tx)
  (juxt (constantly entity) key val (constantly tx)))

(defn rowify [{:keys [entity-attribute tx]} & items]
  (letfn [(->rows [item]
            (map
             (rowify* (find-entity entity-attribute item) tx)
             (dissoc item entity-attribute)))]
    (mapcat ->rows items)))

(defn merge-rows
  ([rows] (merge-rows nil rows))
  ([{:keys [entity? entity-attr? entity-val?]
     :or {entity? identity entity-attr? identity entity-val? identity}} rows]
   (letfn [(row->eav [row]
             (zipmap [::entity ::attribute ::value] row))
           (eav->map [{::keys [entity attribute value]}]
             {entity
              (cond-> {}
                attribute (assoc attribute value)
                :always (into [entity]))})]
     (transduce
      (comp (map row->eav)
            (filter (comp entity? ::entity))
            (filter (comp entity-attr? first ::entity))
            (filter (comp entity-val? second ::entity))
            (map eav->map))
      (partial merge-with merge)
      rows))))

(defn create [rows {:keys [entity-attribute tx]} & items]
  (assert (every? #(-> % (get entity-attribute) not) items))
  (when (seq items)
    (let [db (merge-rows {:entity-attr? #{entity-attribute}} rows)
          next-entities (->> (range)
                             (map (juxt (constantly entity-attribute) identity))
                             (remove (or db {})))]
      (map #(into %1 [%2]) items next-entities))))
