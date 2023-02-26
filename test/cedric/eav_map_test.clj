(ns cedric.eav-map-test
  (:require [clojure.test :refer [deftest is]]
            [cedric.eav-map :as sut]))

(deftest ->map-test
  (is (= {::sut/destroyed-entity [:id 0]}
         (-> (sut/->map {::sut/entity [:id 0] ::sut/attribute :a ::sut/value "v" ::sut/delete-or-destroy :destroy!}) meta))))
