(ns oj.core-test
  (:require [clojure.test :refer :all]
            [oj.core :refer :all]
            [clojure.java.jdbc :as j]))

(defonce test-db
  {:classname "org.h2.Driver"
   :subprotocol "h2:mem"
   :subname "oj-test"})

(defn h2-fixture [f]
  (j/with-db-connection [test-db test-db]
    (f)))

(defn with-test-db [f]
  (h2-fixture (fn [& args]
                (->> (j/create-table-ddl :friends [:name "VARCHAR(100)"]
                                                  [:active :int])
                     (j/db-do-commands test-db))
                (j/insert! test-db :friends {:name "Rupert"})
                (apply f args))))

(use-fixtures :each with-test-db)

(deftest exec-simple-select-query
  (is (= (map #(:name %) (exec {:table :friends} test-db))
         '("Rupert"))))

(deftest exec-insert-query
  (is (= (exec {:table :friends
                :insert {:name "Pearl" :active false}} test-db)
         '(nil)))
  (is (= (count (exec {:table :friends
                       :where {:active false}} test-db))
         1)))

(deftest exec-update-query
  (is (= (exec {:table :friends
                :update {:name "Engelbert"}
                :where {:name "Rupert"}} test-db)
         '(1))))

(deftest exec-delete-query
  (is (= (exec {:table :friends
                :delete true
                :where {:name "Rupert"}} test-db)
         '(1))))
