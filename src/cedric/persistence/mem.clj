(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]))

(defn- key-by-entity [ea items]
  (letfn [(key-by [f coll] (into {} (map (juxt f identity) coll)))]
    (key-by #(find % ea) items)))

#_(defn- create [rows ea & items]
    (letfn [(with-entity [item entity] (into item [entity]))
            (next-entities [ea db]
              (remove
               (or db {})
               (map (juxt (constantly ea) identity) (range))))]
      (let [db        (c/combine {:ea? #{ea}} rows)
            entities  (next-entities ea db)
            new-items (map with-entity items entities)]
        (-> rows
            (concat (mapcat (partial c/->rows {:entity-attribute ea}) new-items))
            (with-meta {::created (key-by-entity ea new-items)})))))

(defrecord Mem [mem]
  Persistence
  (create! [_ props items]
    (-> (apply swap! mem create props items)
        meta ::created)))
