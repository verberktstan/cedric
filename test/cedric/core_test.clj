(ns cedric.core-test
  (:require
   [cedric.core :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest items->rows-test
  (let [item {:a 1 :b 2}]
    (testing "returns rows for items"
      (is (= [[[:a 1] :b 2]] (sut/items->rows :a item)))
      (is (= [[[:a 1] :b 2] [[:a 2] :c 3]]
             (sut/items->rows :a item {:a 2 :c 3}))))
    (testing "throws an error when entity can't be found"
      (let [msg #"Assert failed: entity"]
        (is (thrown-with-msg? Error msg (sut/items->rows :c item)))
        (is (thrown-with-msg? Error msg (sut/items->rows "a" item)))))))

(deftest merge-rows-test
  (let [rows-a [[[:a 1] :b 2]]
        rows-b (concat rows-a [[[:a 1] :c 3]])]
    (testing "returns the items for the rows"
      (is (= {[:a 1] {:a 1 :b 2}}
             (sut/merge-rows rows-a)))
      (is (= {[:a 1] {:a 1 :b 2 :c 3}}
             (sut/merge-rows rows-b))))))

(deftest create-test
  (testing "returns the newly created items (with entity)"
    (is (= [{:a 0 :b 1} {:a 2 :b 3}]
           (sut/create [[[:a 1] :b 2]] :a {:b 1} {:b 3})))))
