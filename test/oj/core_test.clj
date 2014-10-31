(ns oj.core-test
  (:require [clojure.test :refer :all]
            [oj.core :refer :all]))

(deftest select-statement
  (is (= (sqlify {:table :users
                  :select [:username :id]
                  :where {:username "taylorlapeyre" :id 1}
                  :limit 1
                  :order [:id :desc]})
         "SELECT username, id FROM users WHERE username=\"taylorlapeyre\" AND id=1 ORDER BY id desc LIMIT 1")))

(deftest complex-select-statement
  (is (= (sqlify {:table :users
                  :where {:id [1 2 3]}})
         "SELECT * FROM users WHERE id IN (1, 2, 3)")))

(deftest insert-statement
  (is (= (sqlify {:table :users
                  :insert {:username "taylor" :password "password"}})
         "INSERT INTO users (password, username) VALUES (\"password\", \"taylor\")")))

(deftest update-statement
  (is (= (sqlify {:table :users
                  :update {:username "different"}
                  :where {:username "taylor"}})
         "UPDATE users SET username=\"different\" WHERE username=\"taylor\"")))

(deftest delete-statement
  (is (= (sqlify {:table :users
                  :delete true
                  :where {:username "taylor"}})
         "DELETE FROM users WHERE username=\"taylor\"")))
