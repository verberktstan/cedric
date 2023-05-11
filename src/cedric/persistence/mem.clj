(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]))

(defn- assert-items [entity-attribute items]
  (assert (seq items))
  (assert
   (every? identity (map (partial c/find-entity entity-attribute) items))))

(defn- create [mem {:keys [entity-attribute] :as props} & items]
  (let [created (apply c/create (::rows mem) props items)]
    (assert-items entity-attribute created)
    (-> mem
        (update ::rows concat (apply c/rowify props created))
        (assoc ::created created))))

;; Assumes mem is an atom.
(defrecord Mem [mem]
  Persistence
  (create! [_ props items]
    (-> (apply swap! mem create props items) ::created))
  (query [_ props]
    (-> props (c/merge-rows (::rows @mem)) vals c/prune)))
