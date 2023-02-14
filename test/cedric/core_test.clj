(ns cedric.core-test
  (:require [cedric.core :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest ->rows-test
  (let [->rows #(sut/->rows % {:id 0 :attribute "value"})]
    (testing "Returns a collection of rows with entity, attribute and value."
      (is (= [[[:id 0] :attribute "value"]]
             (->rows {:entity-attribute :id})))
      (is (= [[[:attribute "value"] :id 0]]
             (->rows {:entity-attribute :attribute}))))
    (testing "evaluates :gen-entity and uses its result as entity when the
              entity-attribute can't be found"
      (is (= [[[:item/id 2311] :id 0]
              [[:item/id 2311] :attribute "value"]]
             (->rows {:entity-attribute :unknown
                      :gen-entity       (constantly [:item/id 2311])}))))
    (testing "throws an error when the entity can't be found nor generated"
      (is (thrown? AssertionError (->rows {:entity-attribute :unknown}))))))

(deftest combine-test
  (let [rows [[[:id 0] :attribute1 "value1"]
              [[:id 0] :attribute2 "value2"]
              [[:id 1] :attribute3 "value3"]]]
    (is (= {[:id 0] {:id 0 :attribute1 "value1" :attribute2 "value2"}
            [:id 1] {:id 1 :attribute3 "value3"}}
           (sut/combine rows)))))
