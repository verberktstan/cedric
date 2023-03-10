(ns cedric.persistence-test
  (:require
   [cedric.persistence :as sut]
   [cedric.persistence.mem]
   [clojure.test :refer [deftest is testing]])
  (:import
   (cedric.persistence.mem Mem)))

(def make-mem #(Mem. (atom nil)))

(deftest create-test
  (let [db (make-mem)]
    (testing "returns the newly created items (with entity)"
      (is (= [{:a 0 :b 1} {:a 1 :b 2}]
             (sut/create! db {:entity-attribute :a} [{:b 1} {:b 2}]))))
    (testing "returns all the items in db"
      (is (= {[:a 0] {:a 0 :b 1} [:a 1] {:a 1 :b 2}} (sut/query db :all))))
    (testing "returns selected items"
      (is (= {[:a 0] {:a 0 :b 1}} (sut/query db {:entity? #{[:a 0]}}))))))
