(ns oj.core
  (:use oj.generators)
  (:require [clojure.java.jdbc :as j]
            [oj.generators :as gen]
            [oj.validation :as validate]
            [oj.logging :as logging]
            [clojure.string :refer [trim]]))

(def sql-select-generators
  [gen/select
   gen/where
   gen/group
   gen/order
   gen/having
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
  (validate/validate-query-map query)
  (let [generators (cond (:select query) sql-select-generators
                         (:insert query) sql-insert-generators
                         (:update query) sql-update-generators
                         (:delete query) sql-delete-generators
                         :else           sql-select-generators)]
    (->> (for [gen generators]
           (gen query))
         (remove nil?)
         (interpose \space)
         (apply str)
         (trim))))

(defn exec
  "Given a query map and a database config, generates and runs SQL for the query
  and for all join tables. Returns the resuling tuples."
  [query db]
  (logging/pretty-log (sqlify query))

  (letfn [(associate-join [tuple join db]
            (let [[join-name {:keys [table where select]}] join
                  [[foreign-key key]] (vec where)
                  key (if (keyword? key) (key tuple) key)
                  compiled-subquery {:table table
                                     :select select
                                     :where {foreign-key key}}]
              (assoc tuple join-name (exec compiled-subquery db))))]

    (let [tuples (cond (:insert query)
                       (j/insert! db (:table query) (:insert query))

                       (or (:update query) (:delete query))
                       (j/execute! db [(sqlify query)])

                       :else
                       (j/query db [(sqlify query)]))]
      (if-not (:join query)
        tuples
        (for [join (:join query)]
          (for [tuple tuples]
            (associate-join tuple join db)))))))
