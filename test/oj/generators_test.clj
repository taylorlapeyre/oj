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
                    :select [:completed '(avg :price)]
                    :group [:completed]})
           "SELECT completed, avg(price) FROM orders GROUP BY completed"))
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
           "SELECT * FROM users WHERE users.age <= 60 AND users.age >= 18")))

  (testing ":where with the use of a vector of possible values"
    (is (= (sqlify {:table :users
                    :where {:id [1 2 3]}})
           "SELECT * FROM users WHERE users.id IN (1, 2, 3)")))

  (testing "a :where with an empty vector assume NULL."
    (is (= (sqlify {:table :users
                    :where {:id []}})
           "SELECT * FROM users WHERE users.id = NULL"))))

(deftest update-generator
  (testing ":update acting upon a single value"
    (is (= (sqlify {:table :users
                    :update {:username "different"}
                    :where {:username "taylor"}})
           "UPDATE users SET username = 'different' WHERE users.username = 'taylor'")))

  (testing ":update acting upon a multiple values"
    (is (= (sqlify {:table :users
                    :update {:username "different" :updated_at "now"}
                    :where {:username "taylor"}})
           "UPDATE users SET username = 'different', updated_at = 'now' WHERE users.username = 'taylor'"))))

(deftest delete-generator
  (testing ":delete with a boolean and a :where"
    (is (= (sqlify {:table :users
                    :delete true
                    :where {:username "taylor"}})
           "DELETE FROM users WHERE users.username = 'taylor'")))

  (testing ":delete with the :all keyword and no :where statement"
    (is (= (sqlify {:table :users
                    :delete :all})
           "DELETE FROM users"))))

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

(deftest limit-generator
  (testing ":limit with an integer"
    (is (= (sqlify {:table :orders :limit 1})
           "SELECT * FROM orders LIMIT 1"))))

(deftest order-generator
  (testing ":order with a vector containing a col and order"
    (is (= (sqlify {:table :orders :order [:price :asc]})
           "SELECT * FROM orders ORDER BY price asc"))))

(deftest stress-test
  (is (= (sqlify {:table :users
                  :select [:username :id '(avg :score)]
                  :where {:username "taylorlapeyre" :id 1}
                  :limit 1
                  :order [:id :desc]
                  :group [:email]})
         "SELECT username, id, avg(score) FROM users WHERE users.username = 'taylorlapeyre' AND users.id = 1 GROUP BY email ORDER BY id desc LIMIT 1")))


