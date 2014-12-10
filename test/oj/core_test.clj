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

(deftest select-statement
  (is (= (sqlify {:table :users
                  :select [:username :id]
                  :where {:username "taylorlapeyre" :id 1}
                  :limit 1
                  :order [:id :desc]})
         "SELECT username, id FROM users WHERE users.username = 'taylorlapeyre' AND users.id = 1 ORDER BY id desc LIMIT 1")))

(deftest complex-select-statement
  (is (= (sqlify {:table :users
                  :where {:id [1 2 3]}})
         "SELECT * FROM users WHERE users.id IN (1, 2, 3)")))

(deftest select-statement-with-group-by
  (is (= (sqlify {:table :orders
                  :select [:order_id :price]
                  :group [:order_id]})
         "SELECT order_id, price FROM orders GROUP BY order_id")))

(deftest select-statement-with-aggregates
  (is (= (sqlify {:table :orders
                  :select [:order_id '(sum :price)]
                  :group [:order_id]})
         "SELECT order_id, sum(price) FROM orders GROUP BY order_id")))

(deftest insert-statement
  (is (= (sqlify {:table :users
                  :insert {:username "taylor" :password "password"}})
         "INSERT INTO users (password, username) VALUES ('password', 'taylor')")))

(deftest update-statement
  (is (= (sqlify {:table :users
                  :update {:username "different"}
                  :where {:username "taylor"}})
         "UPDATE users SET username = 'different' WHERE users.username = 'taylor'")))

(deftest delete-statement
  (is (= (sqlify {:table :users
                  :delete true
                  :where {:username "taylor"}})
         "DELETE FROM users WHERE users.username = 'taylor'")))

(deftest alternative-delete-statement
  (is (= (sqlify {:table :users
                  :delete :all})
         "DELETE FROM users")))

(deftest alternative-where-statement
  (is (= (sqlify {:table :users
                  :where {:id {:> 2 :< 20 :not= 21}}})
         "SELECT * FROM users WHERE users.id <> 21 AND users.id > 2 AND users.id < 20")))

(deftest equals-where-statement
  (is (= (sqlify {:table :users
                  :where {:id {:>= 2 :<= 20}}})
         "SELECT * FROM users WHERE users.id <= 20 AND users.id >= 2")))

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
