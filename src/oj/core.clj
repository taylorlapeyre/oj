(ns oj.core
  (:use oj.generators)
  (:require [clojure.java.jdbc :as j]
            [oj.generators :as gen]
            [oj.validation :as validate]
            [oj.logging :as logging]
            [clojure.string :refer [trim split]]))

(defn stateful-query?
  "Returns true if the query will have side effects."
  [query]
  (or (:insert query)
      (:update query)
      (:delete query)))

(defn rename-aggregates
  "Given a tuple result, analyzes it for keys that represent the result of
  SQL aggregate functions. When found, converts it to a nested map structure:
  {:sum(price) 500} => {:sum {:price 500}}"
  [tuple]
  (let [aggregate-pair? #(re-matches #"[a-zA-Z]+\([a-zA-Z]+\)" (name (first %)))
        aggregate-pairs (filter aggregate-pair? tuple)
        non-aggregate-pairs (filter (complement aggregate-pair?) tuple)
        aggregate-keys (keys aggregate-pairs)
        aggregate-vals (vals aggregate-pairs)
        format-ops  (comp keyword first #(clojure.string/split % #"\(") name)
        format-cols (comp keyword
                          #(apply str %)
                          drop-last
                          second
                          #(clojure.string/split % #"\(")
                          name)
        ops  (map format-ops  aggregate-keys)
        cols (map format-cols aggregate-keys)]

    (->> (interleave ops cols)
         (partition 2)
         (interleave aggregate-vals)
         (reverse)
         (partition 2)
         (map #(apply assoc-in (cons {} %)))
         (reduce merge)
         (merge (into {} non-aggregate-pairs)))))
 
(def sql-select-generators
  [gen/select
   gen/where
   gen/group
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
                         :else           sql-select-generators)]
    (->> (for [gen generators]
           (gen query))
         (remove nil?)
         (interpose \space)
         (apply str)
         (trim))))

(defn exec
  "Given a query map and a database config, generates and runs SQL for the query.
  Returns the resuling tuples in a humane format."
  [{:keys [table select insert update delete join] :as query} db]
  (logging/pretty-log (sqlify query))
  (let [tuples (cond insert (j/insert! db (:table query) (:insert query))
                     (or update delete) (j/execute! db [(sqlify query)])
                     :else (j/query db [(sqlify query)]))]
    (cond (stateful-query? query) tuples

          (gen/aggregate? (:select query))
          (second (ffirst tuples))

          (and (= 1 (count select)) (gen/aggregate? (first select)))
          (second (ffirst tuples))

          (some gen/aggregate? select)
          (map rename-aggregates tuples)

          :else tuples)))
