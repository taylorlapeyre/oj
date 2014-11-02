(ns oj.validation)

(defn validate-query-map
  [candidate]

  (defn- validate-where []
    (let [where (:where candidate)
          valid-comparator? #(contains? '[= not= < >] %)]
      (when-not where
        (throw (Exception. "[:update, :delete] must contain key: [:where]")))
      (when (map? where)
        (when-not (empty? (filter #(or (vector? %) (list? %)) (vals where)))
          (throw (Exception. "Values for :where must not be vectors or lists."))))))

  (when-not (:table candidate)
    (throw (Exception. "Must contain key: [:table]")))

  (let [n (count (select-keys candidate [:insert :update :delete]))]
    (when (> n 1)
      (throw (Exception. "Cannot contain more than one of the following keys: [:insert :update :delete]"))))

  (when-let [insert (:insert candidate)]
    (when-not (map? insert)
      (throw (Exception. "The corresponding value to :insert must be a map.")))
    (when-not (empty? (filter map? (vals insert)))
      (throw (Exception. "Values for :insert must not be maps.")))
    (when-not (empty? (filter coll? (keys insert)))
      (throw (Exception. "Keys for :insert must not be collections."))))

  (when-let [update (:update candidate)]
    (when-not (map? update)
      (throw (Exception. "The corresponding value to :update must be a map.")))
    (when-not (empty? (filter map? (vals update)))
      (throw (Exception. "Values for :update must not be maps.")))
    (when-not (empty? (filter coll? (keys update)))
      (throw (Exception. "Keys for :update must not be collections.")))
    (validate-where))

  true)
