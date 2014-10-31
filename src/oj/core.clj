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
    (clojure.string/trim (reduce str (interpose \space
                           (for [gen generators]
                             (gen query)))))))

(defn exec [query db]
  (j/query db (sqlify query)))
