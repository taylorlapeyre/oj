(ns oj.generators)

(defn sql-val [value]
  (cond (string? value)
        (str "\"" value "\"")

        (keyword? value)
        (name value)

        (coll? value)
        (reduce str (interpose ", " (map sql-val value)))
        :else value))

(defn select [{:keys [select table]}]
  (str
    (if (empty? select)
      "SELECT *"
      (str "SELECT " (sql-val select)))
    " FROM " (sql-val table)))

(defn limit [{:keys [limit]}]
  (when limit
    (str "LIMIT " limit)))

(defn order [{:keys [order]}]
  (when order
    (if (string? order)
      (str "ORDER BY " order)
      (let [[col direction] order]
        (str "ORDER BY " (sql-val col) \space (sql-val direction))))))

(defn where-clause [[col value]]
   (str (sql-val col)
        (if (coll? value)
          (str " IN (" (sql-val value) ")")
          (str "=" (sql-val value)))))

(defn where [{:keys [where]}]
  (when where
    (str "WHERE "
         (reduce str (interpose " AND "
                                (map where-clause where))))))

(defn insert [{:keys [table insert]}]
  (str
    "INSERT INTO " (sql-val table) " (" (sql-val (keys insert))
    ") VALUES (" (sql-val (vals insert)) ")"))

(defn update [{:keys [table update]}]
  (let [str-keyvals (fn [[col value]]
                      (str
                        (sql-val col) "="
                        (sql-val value)))]

    (str
      "UPDATE " (sql-val table)
      " SET " (reduce str (interpose ", " (map str-keyvals update))))))

(defn delete [{:keys [table]}]
  (str "DELETE FROM " (sql-val table)))
