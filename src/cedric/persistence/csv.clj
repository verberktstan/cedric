(ns cedric.persistence.csv
  (:require [cedric.core :as c]
            [cedric.persistence :refer [Persistence]]
            [clojure.data :as data]
            [clojure.data.csv :as csv]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn- read-rows [filename]
  (with-open [reader (io/reader filename)]
    (map
      (partial mapv edn/read-string)
      (doall
        (csv/read-csv reader :separator \;)))))

;; TODO - DRY this, check persistence-test ns
(defn- write-rows! [filename rows]
  (with-open [writer (io/writer filename :append true)]
    (csv/write-csv writer (map (partial map pr-str) rows) :separator \;)))

(defn- upsert* [filename {:keys  [entity-attribute]
                          ::keys [destroy?]
                          :as    props} item]
  (if destroy?
    (let [entity (find item entity-attribute)]
      (write-rows! filename [[entity (first entity) (second entity) :destroy!]]))
    ;; TODO - DRY this overlap with mem implementation
    (let [found-entity            (find item entity-attribute)
          db                      (c/combine
                                    (if found-entity
                                      {:e? #{found-entity}}
                                      {:ea? #{entity-attribute}})
                                    (read-rows filename))
          entity                  (or found-entity
                                      (c/generate-entity props db))
          [removed added overlap] (data/diff
                                    (get db entity)
                                    (merge item (c/entity->map entity)))
          added-rows              (c/->rows props (c/entity->map added entity))
          removed-props           (assoc props :deleted? true)
          removed-rows            (c/->rows removed-props (c/entity->map removed entity))]
      (write-rows! filename (concat removed-rows added-rows))
      (merge overlap added))))

(defrecord Csv [filename]
  Persistence
  (query [this props]
    ;; TODO - DRY the shared code with mem implementation
    (if (= :all props)
      (c/combine (read-rows filename))
      (c/combine props (read-rows filename))))
  (upsert! [this props item]
    (upsert* filename props item))
  (destroy! [this props item]
    (upsert* filename (assoc props ::destroy? true) item)
    item))
