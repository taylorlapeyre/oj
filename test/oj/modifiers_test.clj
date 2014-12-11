(ns oj.modifiers-test
  (:require [clojure.test :refer :all]
            [oj.modifiers :refer :all]))

(deftest query-modifier
  (is (= (query :users)
         {:table :users})))

(deftest where-modifier
  (is (= (-> (query :users)
             (where {:id 1}))
         {:table :users
          :where {:id 1}})))

(deftest select-modifier
  (is (= (-> (query :users)
             (select [:id]))
         {:table :users
          :select [:id]})))

(deftest insert-modifier
  (is (= (-> (query :users)
             (insert {:email "taylor@mail.com"}))
         {:table :users
          :insert {:email "taylor@mail.com"}})))

(deftest update-modifier
  (is (= (-> (query :users)
             (update {:email "taylor@lol.com"}))
         {:table :users
          :update {:email "taylor@lol.com"}})))

(deftest delete-modifier
  (is (= (-> (query :users) (delete))
         {:table :users
          :delete true})))

(deftest limit-modifier
  (is (= (-> (query :users)
             (limit 1))
         {:table :users
          :limit 1})))

(deftest order-modifier
  (is (= (-> (query :users)
             (order [:id :desc]))
         {:table :users
          :order [:id :desc]})))