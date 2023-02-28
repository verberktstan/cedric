(ns cedric.persistence.csv
  (:require
   [cedric.core :as c]
   [cedric.persistence :refer [Persistence prepare-upsert]]
   [clojure.data.csv :as csv]
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(defn- read-rows [filename]
  (with-open [reader (io/reader filename)]
    (map
      (partial mapv edn/read-string)
      (doall
        (csv/read-csv reader :separator \;)))))

(defn- write-rows! [filename rows]
  (with-open [writer (io/writer filename :append true)]
    (csv/write-csv writer (map (partial map pr-str) rows) :separator \;)))

(defn- upsert* [filename {:keys  [entity-attribute]
                          ::keys [destroy?]
                          :as    props} item]
  (if destroy?
    (let [entity (find item entity-attribute)]
      (write-rows! filename [[entity (first entity) (second entity) :destroy!]]))
    (let [upsert-props (assoc props :get-rows #(read-rows filename))
          data         (prepare-upsert upsert-props item)]
      (write-rows! filename (concat (:deleted-rows data) (:added-rows data)))
      (merge (:overlap data) (:added data)))))

(defrecord Csv [filename]
  Persistence
  (query [this props]
    (if (= :all props)
      (c/combine (read-rows filename))
      (c/combine props (read-rows filename))))
  (upsert! [this props item]
    (upsert* filename props item))
  (destroy! [this props item]
    (upsert* filename (assoc props ::destroy? true) item)
    item))
