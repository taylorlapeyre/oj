(ns oj.validation
  "Functions for validating query maps.")

(defn problem [message]
  (let [message (str "The query map had a problem: " message)]
    (throw (Exception. message))))

(defn validate-query-map [{:keys [table select insert where update delete join]}]
  "Analyzes a query map and throws an error when it finds malformed data."
  ; letfn is really ugly...
  (letfn [(validate-select []
            (when-not (vector? select)
              (problem ":select must be a vector."))
            (when (empty? select)
              (problem "If present, :select must contain at least one column name."))
            (when-not (every? keyword? select)
              (problem "The elements of :select must be keywords."))
            true)

          (validate-insert []
            (when-not (map? insert)
              (problem ":insert must be a map."))
            (when-not (every? keyword? (keys insert))
              (problem "The keys to an :insert must be keywords."))
            (let [valid-type? #(or (string? %) (number? %))]
              (when-not (every? valid-type? (vals insert))
                (problem "Every value to an :insert map must be either a string or a number.")))
            true)

          (validate-where []
            (when-not (map? where)
              (problem ":where must be a map."))
            (when-not (every? keyword? (keys where))
              (problem "The keys to a :where must be keywords."))
            (let [valid-type? #(or (string? %) (number? %))
                  valid-comparator? #(contains? '[= not= < >] %)]
              (for [value (vals where)]
                (if (map? value)
                  (do
                    (when-not (every? valid-comparator? (keys value))
                      (problem "Invalid comparator in :where. Valid keys are [:> :< :not=]"))
                    (when-not (every? valid-type? (vals value))
                      (problem "Every value in a comparator clause must be either a string or a number.")))
                  (when-not (valid-type? value)
                    (problem "Every value in a :where map must be either a string, number, or map.")))))
            true)

          (validate-update []
            (when-not (map? update)
              (problem ":update must be a map."))
            (when-not (every? keyword? (keys update))
              (problem "The keys to an :update must be keywords."))
            (let [valid-type? #(or (string? %) (number? %))]
              (when-not (every? valid-type? (vals update))
                (problem "Every value to an :update map must be either a string or a number.")))
            (when-not where
              (problem ":update requires the presence of a :where key."))
            true)

          (validate-delete []
            (when-not (= delete true)
              (problem ":delete must have the value 'true' to be valid"))
            (when-not where
              (problem ":delete requires the presence of a :where key."))
            true)

          (validate-join []
            (when-not (map? join)
              (problem ":join must be a map."))
            (when-not (every? keyword? (keys join))
              (problem "The keys to a :join must be keywords."))
            (when-not (every? map? (vals join))
              (problem "The values in a :join map must be a map of foreign key pairs."))
            (when-not (every? #(= (count %) 1) (vals join))
              (problem "Foreign map pairs in a :join must have only one key/value."))
            (for [foreign-key-map (vals join)]
              (when-not (and (every? keyword? (vals foreign-key-map))
                             (every? keyword? (keys foreign-key-map)))
                (problem "Both keys and values to foreign key pairs in a :join must be keywords.")))
            true)]
    (when-not (keyword? table)
      (problem ":table is required."))

    (when select (validate-select))
    (when insert (validate-insert))
    (when update (validate-update))
    (when where (validate-where))
    (when delete (validate-delete))
    (when join (validate-join))

    true))
