(ns oj.generators
  "Functions for generating SQL statements from a query map.")

(declare sql-val)

(defn aggregate?
  "Returns whether a given value is an SQL aggregate form."
  [value]
  (and (seq? value)
       (symbol? (first value))))

(defn- sql-aggregate
  "Transforms an SQL aggregate form into its string representation."
  [value]

  (let [[[func] params] (split-at 1 value)]
    (str (name func) "(" (apply str (interpose ", " (map sql-val params))) ")")))

(defn sql-val
  "Takes a value and represents it as it would occur in an SQL query."
  [value]
  (cond (string? value)
        (str "'" value "'")

        (aggregate? value)
        (sql-aggregate value)

        (keyword? value)
        (name value)

        (or (= true value) (= false value))
        (if value 1 0)

        (coll? value)
        (reduce str (interpose ", " (map sql-val value)))
        :else value))

(defn select
  "Generates the SELECT part of a SQL statement from a query map."
  [{:keys [select table]}]
  (str
    (if (empty? select)
      "SELECT *"
      (str "SELECT " (sql-val select)))
    " FROM " (sql-val table)))

(defn limit
  "Generates the LIMIT part of a SQL statement from a query map."
  [{:keys [limit]}]
  (when limit
    (str "LIMIT " limit)))

(defn order
  "Generates the ORDER BY part of a SQL statement from a query map."
  [{:keys [order]}]
  (when-let [[col direction] order]
    (str "ORDER BY " (sql-val col) \space (sql-val direction))))

(defn group
  "Generates the GROUP BY part of a SQL statement from a query map."
  [{:keys [group]}]
  (when group
    (str "GROUP BY " (sql-val group))))

(defn where
  "Generates the WHERE part of a SQL statement from a query map."
  [{:keys [table where]}]
  (letfn [(fully-qualify [col]
            (keyword (str (name table) "." (name col))))

          (where= [col value]
            (str (sql-val (fully-qualify col))
                 (if (coll? value)
                   (str " IN (" (sql-val value) ")")
                   (str " = " (sql-val value)))))

          (where-not= [col value]
            (str (sql-val (fully-qualify col))
                 (if (coll? value)
                   (str " IN (" (sql-val value) ")")
                   (str " <> " (sql-val value)))))

          (where> [col value]
            (str (sql-val (fully-qualify col))
                 (str " > " (sql-val value))))

          (where>= [col value]
            (str (sql-val (fully-qualify col))
                 (str " >= " (sql-val value))))

          (where< [col value]
            (str (sql-val (fully-qualify col))
                 (str " < " (sql-val value))))

          (where<= [col value]
            (str (sql-val (fully-qualify col))
                 (str " <= " (sql-val value))))

          (where-clause [[col predicate]]
            (if (map? predicate)
              (->> (for [[op value] predicate]
                     (case op
                       :> (where> col value)
                       :>= (where>= col value)
                       :< (where< col value)
                       :<= (where<= col value)
                       :not= (where-not= col value)))
                    (interpose " AND ")
                    (reduce str))
              (where= col predicate)))]

    (when where
      (->> (map where-clause where)
           (interpose " AND ")
           (reduce str)
           (str "WHERE ")))))

(defn insert
  "Generates an INSERT SQL statement from a query map."
  [{:keys [table insert]}]

  (str
    "INSERT INTO "
    (sql-val table) " ("
    (sql-val (keys insert))
    ") VALUES ("
    (sql-val (vals insert)) ")"))

(defn update
  "Generates an UPDATE SQL statement from a query map."
  [{:keys [table update]}]
    (->> (for [[col value] update]
           (str (sql-val col) " = "
                (sql-val value)))
         (interpose ", ")
         (reduce str)
         (str " SET ")
         (str "UPDATE " (sql-val table))))

(defn delete
  "Generates a DELETE FROM SQL statement from a query map."
  [{:keys [table]}]
  (str "DELETE FROM " (sql-val table)))
