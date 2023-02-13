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
