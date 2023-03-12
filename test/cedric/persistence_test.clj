(ns cedric.persistence-test
  (:require
   [cedric.persistence :as sut]
   [cedric.persistence.mem]
   [clojure.test :refer [deftest is testing]])
  (:import
   (cedric.persistence.mem Mem)))

(def make-mem #(Mem. (atom nil)))

(deftest create-test
  (let [db (make-mem)
        item0 {:a 0 :b 1}
        item1 {:a 1 :b 2}
        items [item0 item1]]
    (testing "returns the newly created items (with entity)"
      (is (= items (sut/create! db {:entity-attribute :a} [{:b 1} {:b 2}]))))
    (testing "returns all the items in db"
      (is (= items (sut/query db :all))))
    (testing "returns selected items"
      (is (= [item0] (sut/query db {:entity? #{[:a 0]}})))
      (is (= items (sut/query db {:entity-attr? #{:a}})))
      (is (nil? (sut/query db {:entity-attr? #{:z}}))))))

(deftest destroy!-test
  (let [db (make-mem)
        item0 {:a 0 :b 1}
        item1 {:a 1 :b 2}]
    (testing "returns the destroyed item(s)"
      (sut/create! db {:entity-attribute :a} [{:b 1} {:b 2}])
      (is (= [item0] (sut/destroy! db {:entity-attribute :a} #{0})))
      #_(is (= nil @(:mem db)))
      (is (= [item1] (sut/query db :all))))))
