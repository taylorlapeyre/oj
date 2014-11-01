(ns oj.generators)

(defn sql-val
  "Takes a value and represents it as it would occur in an SQL query."
  [value]
  (cond (string? value)
        (str "\"" value "\"")

        (keyword? value)
        (name value)

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

(defn where
  "Generates the WHERE part of a SQL statement from a query map."
  [{:keys [table where]}]

  (defn- fully-qualify
    "Sticks a table and column name together SQL style: table.col"
    [table col]
    (keyword (str (name table) "." (name col))))

  (defn- where=
    [table [col value]]
    (str (sql-val (fully-qualify table col))
         (if (coll? value)
           (str " IN (" (sql-val value) ")")
           (str " = " (sql-val value)))))

  (defn- where-not=
    [table [col value]]
    (str (sql-val (fully-qualify table col))
         (if (coll? value)
           (str " NOT IN (" (sql-val value) ")")
           (str " <> " (sql-val value)))))

  (defn- where>
    [table [col value]]
    (str (sql-val (fully-qualify table col))
         (str " > " (sql-val value))))

  (defn- where<
    [table [col value]]
    (str (sql-val (fully-qualify table col))
         (str " < " (sql-val value))))

  (defn- where-clause
    "Given a column name and a value, generates a valid WHERE clause."
    [table predicate]
    (if (list? predicate)
      (let [[op & args] predicate]
        (case (str op)
          "="    (where= table args)
          ">"    (where> table args)
          "<"    (where< table args)
          "not=" (where-not= table args)))
      (let [[col val] predicate]
        (where= table [col val]))))

  (when where
    (->> (map #(where-clause table %) where)
         (interpose " AND ")
         (reduce str)
         (str "WHERE "))))

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
