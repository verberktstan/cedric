(ns cedric.entity-test
  (:require [cedric.entity :as sut]
            [clojure.test :refer [deftest is]]))

(deftest ->map-test
  (is (= {:id 0} (sut/->map [:id 0])))
  (is (= {:a :b :id 0} (sut/->map {:a :b} [:id 0]))))