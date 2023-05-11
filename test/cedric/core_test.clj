(ns cedric.core-test
  (:require
   [cedric.core :as sut]
   [clojure.test :refer [deftest is testing]]))

(def item {:a 1 :b 2})

(deftest items->rows-test
  (testing "returns rows for items"
    (is (= [[[:a 1] :b 2]] (sut/items->rows :a item)))
    (is (= [[[:a 1] :b 2] [[:a 2] :c 3]]
           (sut/items->rows :a item {:a 2 :c 3}))))
  (testing "throws an error when entity can't be found"
    (is (thrown? AssertionError (sut/items->rows :c item)))
    (is (thrown? AssertionError (sut/items->rows "a" item)))))

#_(deftest destroyed-items->rows-test
  (testing "returns rows for destroyed items"
    (is (= [[[:a 1] nil nil :destroyed]]  (sut/destroyed-items->rows :a item)))))

(deftest merge-rows-test
  (let [rows-a [[[:a 1] :b 2]]
        rows-b (concat rows-a [[[:a 1] :c 3]])]
    (testing "returns the items for the rows"
      (is (= {[:a 1] {:a 1 :b 2}}
             (sut/merge-rows rows-a)))
      (is (= {[:a 1] {:a 1 :b 2 :c 3}}
             (sut/merge-rows rows-b))))
    (let [rows-c (concat rows-b [[[:user/id 0] :user/name "Abraham"]
                                 [[:user/id 1] :user/name "Bobby"]])]
      (testing "filters by entity on predicate :entity?"
        (is (= {[:user/id 0] {:user/id 0 :user/name "Abraham"}}
               (sut/merge-rows {:entity? (comp #{[:user/id 0]})} rows-c))))
      (testing "filters by entity on predicate :entity-attr?"
        (is (= {[:user/id 0] {:user/id 0 :user/name "Abraham"}
                [:user/id 1] {:user/id 1 :user/name "Bobby"}}
               (sut/merge-rows {:entity-attr? (comp #{"user"} namespace)} rows-c)))))))

(deftest create-test
  (testing "returns the newly created items (with entity)"
    (is (= [{:a 0 :b 1} {:a 2 :b 3}]
           (sut/create [[[:a 1] :b 2]] :a {:b 1} {:b 3})))))

#_(deftest destroy-test
  (testing "returns the destroyed items"
    (let [items (sut/destroy [[[:a 0] :b 1]] :a 0)]
      (is (= [[:a 0]] items))
      (is (-> items first meta ::sut/destroyed-entity)))))
