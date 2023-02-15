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

(def ROWS [[[:id 0] :attribute1 "value1"]
           [[:id 1] :attribute1 "value1"]
           [[:id 1] :attribute2 "value2"]])

(deftest read-all-test
  (doseq [init-fn constructors]
    (let [db (init-fn ROWS)]
      (is (= {[:id 0] {:id 0 :attribute1 "value1"}
              [:id 1] {:id 1 :attribute1 "value1" :attribute2 "value2"}}
             (sut/read-all db))))))

(deftest update!-test
  (doseq [init-fn constructors]
    (let [db    (init-fn ROWS)
          props {:entity-attribute :id}
          item  {:id 0 :attribute1 "new-value"}]
      (is (= {:id 0 :attribute1 "new-value"} (sut/update! db props item)))
      (is (= {[:id 0] {:id 0 :attribute1 "new-value"}
              [:id 1] {:id 1 :attribute1 "value1" :attribute2 "value2"}}
             (sut/read-all db))))))
