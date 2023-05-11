(ns cedric.persistence-test
  (:require
   [cedric.persistence :as sut]
   [cedric.persistence.mem]
   [clojure.test :refer [deftest is testing]])
  (:import
   (cedric.persistence.mem Mem)))

(def make-mem #(Mem. (atom nil)))

(deftest create-and-query-test
  (let [db (make-mem)
        item0 {:a 0 :b 1}
        item1 {:a 1 :b 2}
        items [item0 item1]]
    (testing "create!"
      (testing "returns the newly created items (with entity)"
        (is (= items (sut/create! db {:entity-attribute :a} [{:b 1} {:b 2}]))))
      (testing "persists all the data to the db rows"
        (is (= [[[:a 0] :b 1]
                [[:a 1] :b 2]]
             ;; Ignoring the TX value here (the 4th element of each row)
               (map (partial take 3) (sut/rows db))))))
    (testing "query"
      (testing "returns selected items"
        (is (= [item0] (sut/query db {:entity? #{[:a 0]}})))
        (is (= items (sut/query db {:entity-attr? #{:a}})))
        (is (= [item1] (sut/query db {:entity-val? #{1}})))
        (is (nil? (sut/query db {:entity-attr? #{:z}})))))))
