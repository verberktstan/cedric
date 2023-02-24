(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]
            [clojure.data :as data]))

(defn- items-with-entities [db]
  (reduce-kv
    (fn [m entity item]
      (assoc m entity (merge item (c/entity->map entity))))
    nil
    db))

(defn- upsert [rows {:keys  [entity-attribute]
                     ::keys [destroy?]
                     :as    props} item]
  (if destroy?
    (let [entity (find item entity-attribute)]
      (with-meta
        (conj rows [entity (first entity) (second entity) :destroy!]) 
        {::item item}))
    (let [db                      (c/combine (comp #{entity-attribute} first) rows)
          entity                  (if-let [entity (find item entity-attribute)]
                                    entity
                                    (c/generate-entity props db))
          [removed added overlap] (data/diff
                                    (get db entity)
                                    (merge item (c/entity->map entity)))
          added-rows              (c/->rows props (c/entity->map added entity))
          removed-props           (assoc props :deleted? true)
          removed-rows            (c/->rows removed-props (c/entity->map removed entity))]
      (with-meta
        (as-> rows r
          (reduce conj r removed-rows)
          (reduce conj r added-rows))
        {::item (merge-with merge overlap added)}))))

;; Swap and return the ::item from the metadata
(def ^:private swap-item! (comp ::item meta swap!))

(defn- build-entity-pred
  "Returns a function that checks the ea? and ev? predicates for it's input.
  Presumes that v is a vector of 2 elements, the first being the entity attribute,
  the second being the entity value."
  [{:keys [ea? ev?]}]
  (fn entity-pred [v]
    (every?
      #(% v)
      (keep identity [(when ea? (comp ea? first))
                      (when ev? (comp ev? second))]))))

(defrecord Mem [mem]
  Persistence
  (query [this props]
    (if (= :all props)
      (c/combine @mem)
      (c/combine {:entity-pred (build-entity-pred props)} @mem)))
  (upsert! [this props item]
    (swap-item! mem upsert props item))
  (destroy! [this props item]
    (swap-item! mem upsert (assoc props ::destroy? true) item)))
