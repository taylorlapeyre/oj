(ns oj.generators-test
  (:require [clojure.test :refer :all]
            [oj.core :refer :all]))

(deftest select-generator
  (testing "default select when :select isn't present."
    (is (= (sqlify {:table :users})
           "SELECT * FROM users")))

  (testing ":select with a normal field"
    (is (= (sqlify {:table :users :select [:id]})
           "SELECT id FROM users")))

  (testing ":select with aggregate functions"
    (is (= (sqlify {:table :orders
                    :select [:order_id '(avg :price)]})
           "SELECT order_id, avg(price) FROM orders"))
    (is (= (sqlify {:table :orders
                    :select '(sum :price)})
           "SELECT sum(price) FROM orders"))
    (is (= (sqlify {:table :orders
                    :select ['(min :price)]})
           "SELECT min(price) FROM orders"))))

(deftest insert-generator
  (testing ":insert with a map of cols to vals"
    (is (= (sqlify {:table :products
                    :insert {:name "T-Shirt" :color "blue" :collection_id 1}})
           "INSERT INTO products (color, collection_id, name) VALUES ('blue', 1, 'T-Shirt')"))))

(deftest where-generator
  (testing ":where with a map of cols to vals"
    (is (= (sqlify {:table :users
                    :where {:email "taylor@email.com" :age 21}})
           "SELECT * FROM users WHERE users.email = 'taylor@email.com' AND users.age = 21")))

  (testing ":where with the use of comparator functions"
    (is (= (sqlify {:table :users
                    :where {:age {:> 18 :< 60 :not= 21}}})
           "SELECT * FROM users WHERE users.age <> 21 AND users.age > 18 AND users.age < 60")))

  (testing ":where with 'than or equal to' comparators"
    (is (= (sqlify {:table :users
                    :where {:age {:>= 18 :<= 60}}})
           "SELECT * FROM users WHERE users.age <= 60 AND users.age >= 18"))))

(deftest update-generator
  (testing ":update with a vector of ")
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

(deftest group-generator
  (testing ":group with one column name"
    (is (= (sqlify {:table :orders
                    :select [:id]
                    :group [:id]})
           "SELECT id FROM orders GROUP BY id")))

  (testing ":group with multiple column names"
    (is (= (sqlify {:table :orders
                    :select [:id]
                    :group [:id :email]})
           "SELECT id FROM orders GROUP BY id, email"))))

(deftest everything-at-once
  (is (= (sqlify {:table :users
                  :select [:username :id '(avg :score)]
                  :where {:username "taylorlapeyre" :id 1}
                  :limit 1
                  :order [:id :desc]
                  :group [:email]})
         "SELECT username, id, avg(score) FROM users WHERE users.username = 'taylorlapeyre' AND users.id = 1 GROUP BY email ORDER BY id desc LIMIT 1")))


