(ns cedric.persistence.mem
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]))

(defn- assert-items [entity-attribute items]
  (assert (seq items))
  (assert
   (every? identity (map (partial c/find-entity entity-attribute) items))))

(defn- epoch-time[]
  #?(:clj (System/currentTimeMillis)
     :cljs (js/Date.now)))

(defn- wrap-tx [props]
  (assoc props :tx (epoch-time)))

(defn- create [mem props & items]
  (let [{:keys [entity-attribute] :as props} (wrap-tx props)
        created (apply c/create (::rows mem) props items)]
    (assert-items entity-attribute created)
    (-> mem
        (update ::rows concat (apply c/rowify props created))
        (assoc ::created created))))

(defn- destroy [mem {:keys [entity-attribute] :as props} & items]
  (let [items (map #(select-keys % #{entity-attribute}) items)
        props (-> props wrap-tx (assoc :destroyed? true :keep-ea? true))
        destroyed-entities (map (partial c/find-entity entity-attribute) items)
        destroyed-rows (apply  c/rowify props items)]
    (->
     mem
     (update ::rows concat destroyed-rows)
     (assoc ::destroyed (set destroyed-entities)))))

;; Assumes mem is an atom.
(defrecord Mem [mem]
  Persistence
  (create! [_ props items]
    (-> (apply swap! mem create props items) ::created))
  (rows [_]
    (::rows @mem))
  (query [_ props]
    (seq (keep identity (-> props (c/merge-rows (::rows @mem)) vals))))
  (destroy! [_ props items]
    (-> (apply swap! mem destroy props items) ::destroyed)))
