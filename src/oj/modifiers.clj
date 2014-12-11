(ns oj.modifiers
  "Functions that take a query map, change it, and return a new query map.")

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

(defn insert
  "Modifies the query map to include the :insert clause provided"
  [query inserts]
  (assoc query :insert inserts))

(defn update
  "Modifies the query map to include the :update clause provided"
  [query updates]
  (assoc query :update updates))

(defn delete
  "Modifies the query map to include the :delete clause provided"
  [query]
  (assoc query :delete true))
