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
  [entity destroyed? tx]
  (assert entity)
  (assert tx)
  (juxt (constantly entity) key val (constantly destroyed?) (constantly tx)))

(defn rowify [{:keys [entity-attribute tx keep-ea? destroyed?]} & items]
  (letfn [(->rows [item]
            (map
             (rowify* (find-entity entity-attribute item) destroyed? tx)
             (cond-> item
               (not keep-ea?) (dissoc entity-attribute))))]
    (mapcat ->rows items)))

(defn- tx-after?
  "Returns a predicate function, that checks if ::tx is greater than supplied :tx?"
  [{:keys [tx?]}]
  (if tx?
    (comp (partial < tx?) ::tx)
    (constantly false)))

(defn- row->eavt
  "Returns a eavt map containing the entity, attribute, value and transaction
  for a given row."
  [row]
  (zipmap [::entity ::attribute ::value ::tx] row))

(defn- eav->map
  "Retuns a map with the item keyed by it's entity."
  [{::keys [entity attribute value]}]
  {entity
   (cond-> {}
     attribute (assoc attribute value)
     :always (into [entity]))})

(defn merge-rows
  "Returns a map with all items based off the supplied rows, keyed by their
  respective entity."
  ([rows] (merge-rows nil rows))
  ([{:keys [entity? entity-attr? entity-val?]
     :or {entity? identity entity-attr? identity entity-val? identity}
     :as props} rows]
   (transduce
    (comp (map row->eavt)
          (remove (tx-after? props))
          (filter (comp entity? ::entity))
          ;; This relies on the fact that the entity is a vector of
          ;; [entity-attr entity-val] (much like a map-entry)
          (filter (comp entity-attr? first ::entity))
          (filter (comp entity-val? second ::entity))
          (map eav->map))
    (partial merge-with merge)
    rows)))

(defn create
  "Returns the items with a new entity-value associated with entity-attribute."
  [rows {:keys [entity-attribute]} & items]
  (assert (every? #(-> % (get entity-attribute) not) items))
  (assert (seq items))
  (let [db (merge-rows {:entity-attr? #{entity-attribute}} rows)
        ;; Generate a lazy seq of the next available entities.
        next-entities (->> (range)
                           (map (juxt (constantly entity-attribute) identity))
                           (remove (or db {})))]
    (map (fn [item entity] (into item [entity])) items next-entities)))
