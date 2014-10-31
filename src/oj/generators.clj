(ns oj.generators)

(defn sql-val [value]
  (cond (string? value)
        (str "\"" value "\"")

        (keyword? value)
        (name value)

        (coll? value)
        (reduce str (interpose ", " (map sql-val value)))
        :else value))

(defn generate-select [{:keys [select]}]
  (if (empty? select)
    "SELECT *"
    (str "SELECT " (sql-val select))))

(defn generate-from [{:keys [table]}]
  (str "FROM " (sql-val table)))

(defn generate-limit [{:keys [limit]}]
  (when limit
    (str "LIMIT " limit)))

(defn generate-order [{:keys [order]}]
  (when order
    (if (string? order)
      (str "ORDER BY " order)
      (let [[col direction] order]
        (str "ORDER BY " (sql-val col) \space (sql-val direction))))))

(defn where-clause [[col value]]
   (str (sql-val col)
        (if (coll? value)
          (str " IN (" (reduce str (interpose \space value)) ")")
          (str "=" (sql-val value)))))

(defn generate-where [{:keys [where]}]
  (when where
    (str "WHERE "
         (reduce str (interpose " AND "
                                (map where-clause where))))))

(defn generate-insert [{:keys [table insert]}]
  (str
    "INSERT INTO " (sql-val table) " (" (sql-val (keys insert))
    ") VALUES (" (sql-val (vals insert)) ")"))

(defn generate-update [{:keys [table update]}]
  (let [str-keyvals (fn [[col value]]
                      (str
                        (sql-val col) "="
                        (sql-val value)))]

    (str
      "UPDATE " (sql-val table)
      " SET " (reduce str (interpose ", " (map str-keyvals update))))))

(defn generate-delete [{:keys [table]}]
  (str "DELETE FROM " (sql-val table)))
