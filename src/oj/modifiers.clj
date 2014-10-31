(ns oj.modifiers)

(defn query
  "Creates a skeletal query map with the given table name."
  [table-name]
  {:table table-name})

(defn where
  "Modifies the query map to include the :where clause provided"
  [query wheres]
  (assoc query :where wheres))

(defn offset
  "Modifies the query map to include an :offset"
  [query n]
  (assoc query :offset n))

(defn order
  "Modifies the query map to include the :order clause provided"
  [query order-by]
  (assoc query :order order-by))

(defn select
  "Modifies the query map to include the :select clause provided"
  [query columns]
  (assoc query :select columns))
