(ns cedric.persistence-test
  (:require [cedric.persistence :as sut]
            [cedric.persistence.csv]
            [cedric.persistence.mem]
            [clojure.java.shell :refer [sh]]
            [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv])
  (:import (cedric.persistence.csv Csv)
           (cedric.persistence.mem Mem)
           (java.util Date)))

(defn- write-rows! [filename rows]
  (with-open [writer (io/writer filename :append true)]
    (csv/write-csv writer (map (partial map pr-str) rows) :separator \;)))

(defn- prepare-csv-file! [& [rows]]
  (let [dir      "/tmp/"
        time     (.toString (.toInstant (Date. )))
        filename (str dir (gensym time) ".csv")]
    (sh "mkdir" dir)
    (sh "rm" filename)
    (sh "touch" filename)
    (when rows
      (write-rows! filename rows))
    filename))

;; Test all implementations, only the constructor is different
(def make-mem  (fn [& [rows]] (Mem. (atom rows))))
(def make-csv (fn [ & [rows]] (Csv. (prepare-csv-file! rows))))
(def constructors [make-mem make-csv])

(def ROWS [[[:id 0] :attribute1 "value1" nil]
           [[:id 1] :attribute1 "value1" nil]
           [[:id 1] :attribute2 "value2" nil]])

(deftest query-test
  (doseq [init-fn constructors]
    (let [db    (init-fn ROWS)
          items {[:id 0] {:id 0 :attribute1 "value1"}
                 [:id 1] {:id 1 :attribute1 "value1" :attribute2 "value2"}}]
      (is (= items (sut/query db :all)))
      (is (= {[:id 0] {:id 0 :attribute1 "value1"}}
             (sut/query db {:ev? #{0}})))
      (is (= {} (sut/query db {:ea? #{:user}})))
      (is (= items (sut/query db {:ea? #{:id}}))))))

(deftest upsert!-test
  (doseq [init-fn constructors]
    (testing "upsert!"
      (let [props     {:entity-attribute :id}
            base-item {:attribute "value"}
            item2     (merge base-item {:id 2})]
        (testing "returns and saves a new item with a new entity attribute associated"
          (let [db (init-fn ROWS)]
            (is (= item2 (sut/upsert! db props base-item)))
            (is (= (merge base-item {:id 2})
                   (get (sut/query db :all) [:id 2])))))

        (testing "returns and saves the updated attributes of an item"
          (let [db   (init-fn ROWS)
                item {:id 0 :attribute1 "new-value"}]
            (is (= item (sut/upsert! db props item)))
            (is (= {:id 0 :attribute1 "new-value"}
                   (get (sut/query db :all) [:id 0])))))

        (testing "returns and saves the deleted attributes of an item"
          (let [db    (init-fn ROWS)
                item1 {:id 1 :attribute1 "value1"}]
            (is (= item1 (sut/upsert! db props item1)))
            (is (= {:id 1 :attribute1 "value1"}
                   (get (sut/query db :all) [:id 1])))))))))

(deftest destroy!-test
  (doseq [init-fn constructors]
    (let [db    (init-fn ROWS)
          props {:entity-attribute :id}
          item  {:id 1}]
      (is (= item (sut/destroy! db props item)))
      (is (= {[:id 0] {:id 0 :attribute1 "value1"}}
             (sut/query db :all))))))
