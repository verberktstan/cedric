(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [prepare-upsert Persistence]]
            [cedric.entity :as entity]
            [clojure.data :as data]))

(defn- items-with-entities [db]
  (reduce-kv
    (fn [m entity item]
      (assoc m entity (merge item (entity/->map entity))))
    nil
    db))

(defn- upsert [rows {:keys  [entity-attribute]
                     ::keys [destroy?]
                     :as    props} item]
  (if destroy?
    (when-let [entity (find item entity-attribute)]
      (with-meta
        (conj rows [entity (first entity) (second entity) :destroy!]) 
        {::item item}))
    (let [upsert-props (assoc props :get-rows (constantly rows))
          data         (prepare-upsert upsert-props item)]
      (with-meta
        (as-> rows r
          (reduce conj r (:deleted-rows data))
          (reduce conj r (:added-rows data)))
        {::item (merge-with merge (:overlap data) (:added data))}))))

;; Swap and return the ::item from the metadata
(def ^:private swap-item! (comp ::item meta swap!))

(defrecord Mem [mem]
  Persistence
  (query [this props]
    (if (= :all props)
      (c/combine @mem)
      (c/combine props @mem)))
  (upsert! [this props item]
    (swap-item! mem upsert props item))
  (destroy! [this props item]
    (swap-item! mem upsert (assoc props ::destroy? true) item)))
