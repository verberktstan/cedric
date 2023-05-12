(ns cedric.core-test
  (:require
   [cedric.core :as sut]
   [clojure.test :refer [deftest is testing]]))

(def item {:a 1 :b 2})

(deftest rowify-test
  (let [tx 999
        props {:entity-attribute :a :tx tx}]
    (testing "returns rows for items"
      (is (= [[[:a 1] :b 2 nil tx]] (sut/rowify props item)))
      (is (= [[[:a 1] :b 2 nil tx] [[:a 2] :c 3 nil tx]]
             (sut/rowify props item {:a 2 :c 3})))
      (is (= [[[:a 1] :b 2 true tx]] (sut/rowify (assoc props :destroyed? true) item))))
    (testing "throws an error when entity can't be found"
      (is (thrown?
           #?(:clj AssertionError :cljs js/Error)
           (sut/rowify (assoc props :entity-attribute :c) item)))
      (is (thrown?
           #?(:clj AssertionError :cljs js/Error)
           (sut/rowify (assoc props :entity-attribute "a") item))))))

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
               (sut/merge-rows {:entity-attr? (comp #{"user"} namespace)} rows-c))))))
  (let [rows-c [[[:user/id 1] :user/name "Name One" nil 99]]
        rows-d (concat rows-c [[[:user/id 2] :user/name "Name Two" nil 100]])]
    (testing "doesn't return items with TX above the requested TX"
      (is (= {[:user/id 1] {:user/id 1 :user/name "Name One"}
              [:user/id 2] {:user/id 2 :user/name "Name Two"}}
             (sut/merge-rows rows-d)))
      (is (= {[:user/id 1] {:user/id 1 :user/name "Name One"}}
             (sut/merge-rows {:tx? 99} rows-d))))))

(deftest create-test
  (testing "returns the newly created items (with entity)"
    (is (= [{:a 0 :b 1} {:a 2 :b 3}]
           (sut/create
             [[[:a 1] :b 2]]
             {:entity-attribute :a
              :tx 999}
             {:b 1} {:b 3})))))
