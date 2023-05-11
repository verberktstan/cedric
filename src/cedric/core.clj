(ns cedric.core)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CEDRIC - the Cedric Event DRIven datapersistence Companion
;; Store associatve data (maps) as rows in a EAV database.
;; Backends implemented as in-memory db (TODO - csv, edn and SQLite implementations)

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

(defn- tx-after? [{:keys [tx?]}]
  (if tx?
    (comp (partial < tx?) ::tx)
    (constantly false)))

(defn merge-rows
  ([rows] (merge-rows nil rows))
  ([{:keys [entity? entity-attr? entity-val?]
     :or {entity? identity entity-attr? identity entity-val? identity}
     :as props} rows]
   (letfn [(row->eavt [row]
             (zipmap [::entity ::attribute ::value ::tx] row))
           (eav->map [{::keys [entity attribute value]}]
             {entity
              (cond-> {}
                attribute (assoc attribute value)
                :always (into [entity]))})]
     (transduce
      (comp (map row->eavt)
            (remove (tx-after? props))
            (filter (comp entity? ::entity))
            (filter (comp entity-attr? first ::entity)) ;; This relies on the fact that the entity is a vector of [entity-attr entity-val] (much like a map-entry)
            (filter (comp entity-val? second ::entity))
            (map eav->map))
      (partial merge-with merge)
      rows))))

(defn create [rows {:keys [entity-attribute #_tx]} & items]
  (assert (every? #(-> % (get entity-attribute) not) items))
  (when (seq items)
    (let [db (merge-rows {:entity-attr? #{entity-attribute}} rows)
          next-entities (->> (range)
                             (map (juxt (constantly entity-attribute) identity))
                             (remove (or db {})))]
      (map (fn [item entity] (into item [entity])) items next-entities))))
