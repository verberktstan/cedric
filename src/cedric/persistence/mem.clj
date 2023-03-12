(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]))

(defn- create [mem {:keys [entity-attribute]} & items]
  (assert (keyword? entity-attribute))
  (let [created (apply c/create (::rows mem) entity-attribute items)]
    (-> mem
        (update ::rows concat (apply c/items->rows entity-attribute created))
        (assoc ::created created))))

;; Assumes mem is an atom.
(defrecord Mem [mem]
  Persistence
  (create! [_ props items]
    (-> (apply swap! mem create props items)
        ::created))
  (query [_ props]
    (vals (c/merge-rows props (::rows @mem)))))
