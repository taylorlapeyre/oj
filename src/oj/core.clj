(ns oj.core
  (:use oj.generators)
  (:require [clojure.java.jdbc :as j]))

(def sql-select-generators
  [generate-select
   generate-from
   generate-where
   generate-order
   generate-limit])

(def sql-insert-generators
  [generate-insert])

(def sql-update-generators
  [generate-update
   generate-where])

(def sql-delete-generators
  [generate-delete
   generate-where])

(defn sqlify [query]
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
    (reduce str (interpose \space
                           (for [gen generators]
                             (gen query))))))

(defn exec [query db]
  (j/query db (sqlify query)))
