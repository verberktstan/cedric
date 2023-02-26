(ns cedric.eav-map
  (:require [cedric.entity :as entity]))

(def zip (partial zipmap [::entity ::attribute ::value ::delete-or-destroy]))

(defn make [entity delete]
  (comp zip (juxt (constantly entity) key val (constantly delete))))

(def ->row (juxt ::entity ::attribute ::value ::delete-or-destroy))

(defn ->map [{::keys [entity attribute value delete-or-destroy]}]
  (let [av-map {attribute value}]
    (case delete-or-destroy
      :destroy! (with-meta {} {::destroyed-entity entity})
      :delete!  {entity (with-meta av-map {::deleted-attribute attribute})}
      {entity (entity/->map av-map entity)})))
