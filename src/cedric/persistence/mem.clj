(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]
            [clojure.data :as data]))

(defn- create [rows {:keys [entity-attribute] :as props} item]
  (let [db                (c/combine rows)
        gen-entity        (c/make-entity-generator db)
        entity            (or (find item entity-attribute)
                              (gen-entity entity-attribute))
        item-rows         (c/->rows props (merge item (into {} [entity])))
        [_ added overlap] (data/diff db (->> item-rows (map c/zip-eav) c/->map))]
    (with-meta
      (reduce conj rows (mapcat (partial c/->rows props) (vals added)))
      {::item (get (merge-with merge added overlap) entity)})))

(defrecord Mem [mem]
  Persistence
  (create! [this props item]
    (-> mem
        (swap! create props item)
        meta
        ::item))
  (read-all [this] (c/combine @mem)))
