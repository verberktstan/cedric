(ns cedric.persistence-test
  (:require [clojure.test :refer [deftest is testing]]
            [cedric.persistence :as sut]
            [cedric.persistence.mem :as mem])
  (:import (cedric.persistence.mem Mem)))

;; Test all implementations, only the constructor is different
(def make-mem  (fn [& [rows]] (Mem. (atom rows))))
(def constructors [make-mem])

(def ROWS [[[:id 0] :attribute1 "value1" nil]
           [[:id 1] :attribute1 "value1" nil]
           [[:id 1] :attribute2 "value2" nil]])

(deftest query-test
  (doseq [init-fn constructors]
    (let [db (init-fn ROWS)]
      (is (= {[:id 0] {:id 0 :attribute1 "value1"}
              [:id 1] {:id 1 :attribute1 "value1" :attribute2 "value2"}}
             (sut/query db :all))))))

(deftest upsert!-test
  (doseq [init-fn constructors]
    (testing "upsert!"
      (let [props     {:entity-attribute :id}
            base-item {:attribute "value"}
            item2     (merge base-item {:id 2})]
        (testing "returns and saves a new item with a new entity attribute associated"
          (let [db (init-fn ROWS)]
            (is (= item2 (sut/upsert! db props base-item)))
            (is (= (merge base-item {:id 2})
                   (get (sut/query db :all) [:id 2])))))

        (testing "returns and saves the updated attributes of an item"
          (let [db   (init-fn ROWS)
                item {:id 0 :attribute1 "new-value"}]
            (is (= item (sut/upsert! db props item)))
            (is (= {:id 0 :attribute1 "new-value"}
                   (get (sut/query db :all) [:id 0])))))

        (testing "returns and saves the deleted attributes of an item"
          (let [db    (init-fn ROWS)
                item1 {:id 1 :attribute1 "value1"}]
            (is (= item1 (sut/upsert! db props item1)))
            (is (= {:id 1 :attribute1 "value1"}
                   (get (sut/query db :all) [:id 1])))))))))

(deftest destroy!-test
  (doseq [init-fn constructors]
    (let [db    (init-fn ROWS)
          props {:entity-attribute :id}
          item  {:id 1}]
      (is (= item (sut/destroy! db props item)))
      (is (= {[:id 0] {:id 0 :attribute1 "value1"}}
             (sut/query db :all))))))
