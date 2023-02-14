(ns cedric.persistence-test
  (:require [clojure.test :refer [deftest is]]
            [cedric.persistence :as sut]
            [cedric.persistence.mem :as mem])
  (:import (cedric.persistence.mem Mem)))

;; Test all implementations, only the constructor is different
(def constructors [#(Mem. (atom nil))])

(deftest create!-test
  (let [props {:entity-attribute :id}
        item  {:attribute "value"}]
    (doseq [init-fn constructors]
      (let [db (init-fn)]
        (is (= (merge item {:id 0}) (sut/create! db props item))))
      (let [db (init-fn)]
        (sut/create! db props item)
        (is (= (merge item {:id 1}) (sut/create! db props item)))))))
