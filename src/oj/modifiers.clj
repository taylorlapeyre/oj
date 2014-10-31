(ns oj.modifiers)

(defn query [table-name]
  {:table table-name})

(defn where [query wheres]
  (assoc query :where wheres))

(defn offset [query n]
  (assoc query :offset n))

(defn order [query order-by]
  (assoc query :order order-by))

(defn select [query columns]
  (assoc query :select columns))
