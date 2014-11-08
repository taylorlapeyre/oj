(ns oj.core
  (:use oj.generators)
  (:require [clojure.java.jdbc :as j]
            [oj.generators :as gen]
            [oj.validation :as validate]
            [clojure.string :refer [trim]]))

(def connections (atom {}))

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
  (validate/validate-query-map query)
  (let [generators (cond (:select query) sql-select-generators
                         (:insert query) sql-insert-generators
                         (:update query) sql-update-generators
                         (:delete query) sql-delete-generators
                         :else sql-select-generators)]
    (trim (reduce str (interpose \space
                      (for [gen generators]
                        (gen query)))))))

(defn exec
  "Given a query map and a database config, generates and runs SQL for the query
  and for all join tables. Returns the resuling tuples."
  [query db]
  (println (sqlify query))

  (letfn [(associate-join [query-result join db]
            (let [[join-name {:keys [table where select]}] join
                  [[foreign-key key]] (vec where)
                  key (if (keyword? key) (key query-result) key)
                  compiled-subquery {:table table
                                     :select select
                                     :where {foreign-key key}}]
              (assoc query-result join-name (exec compiled-subquery db))))]
    (let [connection (if (get db @connections) (get db @connections)
                       (let [conn (j/get-connection db)]
                         (swap! connections assoc db conn)
                         conn))
          jdbc-fn (if (or (:insert query)
                          (:update query)
                          (:delete query)) j/execute! j/query)
          tuples (j/with-db-connection [connection db]
                   (jdbc-fn db [(sqlify query)]))]
      (if-not (:join query) tuples
        (for [join (:join query)]
          (for [tuple tuples]
            (associate-join tuple join db)))))))
