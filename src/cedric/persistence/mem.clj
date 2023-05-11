(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]))

(defn- assert-items [entity-attribute items]
  (assert (seq items))
  (assert
   (every? identity (map (partial c/find-entity entity-attribute) items))))

;; TODO - Make tx generation work in JS as well as Java!
(defn- wrap-tx [props]
  (assoc props :tx (System/currentTimeMillis)))

(defn- create [mem props & items]
  (let [{:keys [entity-attribute] :as props} (wrap-tx props)
        created (apply c/create (::rows mem) props items)]
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
    (-> props (c/merge-rows (::rows @mem)) vals)))
