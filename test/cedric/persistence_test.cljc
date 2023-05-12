(ns cedric.persistence-test
  (:require
   [cedric.persistence :as sut]
   #?(:clj [cedric.persistence.mem]
      :cljs [cedric.persistence.mem :refer [Mem]])
   [clojure.test :refer [deftest is testing]])
  #?(:clj (:import (cedric.persistence.mem Mem))))

(def make-mem #(Mem. (atom nil)))

(defn- fill-db [db]
  (sut/create! db {:entity-attribute :a} [{:b 1} {:b 2}]))

(deftest create!-and-query-test
  (let [db (make-mem)
        item0 {:a 0 :b 1}
        item1 {:a 1 :b 2}
        items [item0 item1]]
    (testing "create!"
      (testing "returns the newly created items (with entity)"
        (is (= items (fill-db db))))
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

(deftest destroy!-test
  (testing "destroy!"
    (let [db (doto (make-mem) fill-db)]
      (testing "returns the entities of the destroyed items"
        (is (= #{[:a 0]} (sut/destroy! db {:entity-attribute :a} [{:a 0}]))))
      (testing "marks the rows as destroyed"
        (is (= [[[:a 0] :b 1 nil]
                [[:a 1] :b 2 nil]
                [[:a 0] :a 0 true]]
               (map (partial take 4) (sut/rows db))))))))
