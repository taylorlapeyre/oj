(ns oj.validation
  "Functions for validating query maps.")

(defn problem [message]
  (let [message (str "The query map had a problem: " message)]
    (throw (Exception. message))))

(def valid-comparator?
  #{:> :< :>= :<= :not=})

(def valid-aggregate-function?
  #{'count 'avg 'max 'min 'first 'last 'sum})

(defn validate-query-map [{:keys [table select insert where group update delete join]}]
  "Analyzes a query map and throws an error when it finds malformed data."
  ; letfn is really ugly...
  (letfn [(validate-select []
            (when-not (or (seq? select) (vector? select))
              (problem ":select must be a seq or a vector."))
            (when (empty? select)
              (problem ":select must not be empty when present."))
            (let [valid-type? #(or (keyword? %) (symbol? %) (seq? %))]
              (when-not (every? valid-type? select)
                (problem "The elements of :select must be keywords, symbols, or seqs.")))
            (when-not (if (seq? select)
                        (valid-aggregate-function? (first select))
                        (every? valid-aggregate-function? (map first (filter seq? select))))
              (problem "Invalid aggregate function. Valid functions are [count avg max min first last sum]."))
            true)

          (validate-insert []
            (when-not (map? insert)
              (problem ":insert must be a map."))
            (when (empty? insert)
              (problem ":insert must not be empty when present."))
            (when-not (every? keyword? (keys insert))
              (problem "The keys to an :insert must be keywords."))
            (let [valid-type? #(or (string? %) (number? %) (= true %) (= false %))]
              (when-not (every? valid-type? (vals insert))
                (problem "Every value to an :insert map must be either a string, number, or boolean.")))
            true)

          (validate-where []
            (when-not (map? where)
              (problem ":where must be a map."))
            (when (empty? where)
              (problem ":where must not be empty when present."))
            (when-not (every? keyword? (keys where))
              (problem "The keys to a :where must be keywords."))
            (let [valid-type? #(or (string? %) (number? %))]
              (for [value (vals where)]
                (cond (map? value)
                      (do (when-not (every? valid-comparator? (keys value))
                            (problem "Invalid comparator in :where. Valid keys are [:> :< :not=]"))
                          (when-not (every? valid-type? (vals value))
                            (problem "Every value in a comparator clause must be either a string or a number.")))
                      (vector? value)
                      (when-not (every? valid-type? value)
                        "Every value in a range of values must be either a string or a number.")
                      (not (valid-type? value))
                      (problem "Every value in a :where map must be either a string, number, vector, or map."))))
            true)

          (validate-group []
            (when-not (vector? group)
              (problem ":group must be a vector."))
            (when (empty? group)
              (problem ":group must not be empty when present."))
            (when-not (every? keyword? group)
              (problem "Every value in a :group must be a keyword")))

          (validate-update []
            (when-not (map? update)
              (problem ":update must be a map."))
            (when (empty? update)
              (problem ":update must not be empty when present."))
            (when-not (every? keyword? (keys update))
              (problem "The keys to an :update must be keywords."))
            (let [valid-type? #(or (string? %) (number? %) (= true %) (= false %))]
              (when-not (every? valid-type? (vals update))
                (problem "Every value to an :update map must be either a string, number, or boolean.")))
            (when-not where
              (problem ":update requires the presence of a :where key."))
            true)

          (validate-delete []
            (when-not delete
              (problem ":delete must have a truthy value."))
            (when (and (not= delete :all) (nil? where))
              (problem ":delete requires the presence of a :where key when its value is not :all."))
            true)]

    (when-not (keyword? table)
      (problem ":table is required."))

    (when select (validate-select))
    (when insert (validate-insert))
    (when update (validate-update))
    (when group (validate-group))
    (when where (validate-where))
    (when delete (validate-delete))

    true))
