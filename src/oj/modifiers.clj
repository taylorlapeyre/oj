(ns oj.modifiers
  "Functions that take a query map, change it, and return a new query map."
  (:use inflections.core))

(defn query
  "Creates a skeletal query map with the given table name."
  [table-name]
  {:table table-name})

(defn where
  "Modifies the query map to include the :where clause provided"
  [query wheres]
  (assoc query :where wheres))

(defn limit
  "Modifies the query map to include an :limit"
  [query n]
  (assoc query :limit n))

(defn order
  "Modifies the query map to include the :order clause provided"
  [query order-by]
  (assoc query :order order-by))

(defn select
  "Modifies the query map to include the :select clause provided"
  [query columns]
  (assoc query :select columns))

(defn join
  "Modifies the query map to include the :join clause provided. If no join
  columns are specified, it will make a guess:
    (singlular :table)_id => id"
  ([q table on]
    (assoc-in q [:join (keyword table)]
      (-> (query table)
          (where on))))
  ([q table]
    (assoc-in q [:join (keyword table)]
      (let [foreign-key (str (name (singular (:table q))) "_id")]
        (-> (query table)
            (where {(keyword foreign-key) :id}))))))
