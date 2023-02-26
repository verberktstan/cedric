(ns cedric.entity)

(defn ->map
  "Returns a map with the entity as part of it.
  `(->map {:a :b} [:id 0]) => {:a :b :id 0}`"
  ([entity] (->map nil entity))
  ([m entity]
   (into (or m {}) [entity])))
