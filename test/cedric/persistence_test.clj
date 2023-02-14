(ns cedric.persistence-test
  (:require [clojure.test :refer [deftest is]]
            [cedric.persistence :as sut]
            [cedric.persistence.mem :as mem])
  (:import (cedric.persistence.mem Mem)))

;; Test all implementations, only the constructor is different
(def make-mem  (fn [& [rows]] (Mem. (atom rows))))
(def constructors [make-mem])

(deftest create!-test
  (let [props {:entity-attribute :id}
        item  {:attribute "value"}]
    (doseq [init-fn constructors]
      (let [db (init-fn)]
        (is (= (merge item {:id 0}) (sut/create! db props item))))
      (let [db (init-fn)]
        (sut/create! db props item)
        (is (= (merge item {:id 1}) (sut/create! db props item)))))))

(deftest read-all-test
  (doseq [init-fn constructors]
    (let [rows [[[:id 0] :attribute1 "value1"]
                [[:id 1] :attribute1 "value1"]
                [[:id 1] :attribute2 "value2"]]
          db   (init-fn rows)]
      (is (= (cedric.core/combine rows) (sut/read-all db))))))
