(ns oj.core
  (:use oj.generators)
  (:require [clojure.java.jdbc :as j]
            [oj.generators :as gen]))

(def sql-select-generators
  [gen/select
   gen/where
   gen/order
   gen/limit])

(def sql-insert-generators
  [gen/insert])

(def sql-update-generators
  [gen/update
   gen/where])

(def sql-delete-generators
  [gen/delete
   gen/where])

(defn sqlify
  "Takes a query map and returns a valid SQL statement to be executed."
  [query]
  (let [generators (cond (:select query)
                         sql-select-generators
                         (:insert query)
                         sql-insert-generators
                         (:update query)
                         sql-update-generators
                         (:delete query)
                         sql-delete-generators
                         :else
                         sql-select-generators)]
    (clojure.string/trim (reduce str (interpose \space
                           (for [gen generators]
                             (gen query)))))))

(defn exec
  "Given a query map and a database config, executes the generated SQL
  and returns the result."
  [query db]
  (j/query db (sqlify query)))
