(ns oj.core-test
  (:require [clojure.test :refer :all]
            [oj.core :refer :all]
            [clojure.java.jdbc :as j]))

; Setup
; ==========================================

(def dummy-data
  [{:id 1 :name "Rubix Cube" :published 1 :price 25}
   {:id 2 :name "Play Set" :published 1 :price 50}
   {:id 3 :name "Xbox" :published 1 :price 300}
   {:id 4 :name "Legos" :published 1 :price 30}
   {:id 5 :name "iPhone 4" :published 0 :price 199}])

(defonce db
  {:classname "org.h2.Driver"
   :subprotocol "h2:mem"
   :subname "oj-test"})

(defn h2-fixture [f]
  (j/with-db-connection [db db]
    (f)))

(defn with-test-db [f]
  (h2-fixture
    (fn [& args]
      (->> (j/create-table-ddl :items [:id :int :primary :key]
                                      [:name "VARCHAR(100)"]
                                      [:published :int]
                                      [:price :int])
           (j/db-do-commands db))
      (j/insert! db :items (nth dummy-data 0))
      (j/insert! db :items (nth dummy-data 1))
      (j/insert! db :items (nth dummy-data 2))
      (j/insert! db :items (nth dummy-data 3))
      (j/insert! db :items (nth dummy-data 4))
      (apply f args))))

(use-fixtures :each with-test-db)


; Select Queries
; ==========================================

(deftest select-queries
  (testing "default select when :select isn't present."
    (let [query-map {:table :items}
          result (exec query-map db)]
      (is (= (count result) 5))
      (is (= (first result) (first dummy-data)))))

  (testing ":select with a normal field"
    (let [query-map {:table :items :select [:id]}
          result (exec query-map db)]
      (is (= (count result) 5))
      (is (= (first result) (select-keys (first dummy-data) [:id])))))

  (testing ":select with aggregate functions"
    (let [query-map {:table :items
                     :select [:published '(avg :price)]
                     :group [:published]}
          result (exec query-map db)]
      (is (= (count result) 2))
      (is (= (get-in (first result) [:avg :price]) 101)))

    (let [query-map {:table :items
                     :select '(avg :price)}
          result (exec query-map db)]
      (is (integer? result))
      (is (= result 120)))

    (let [query-map {:table :items
                     :select ['(avg :price)]}
          result (exec query-map db)]
      (is (integer? result))
      (is (= result 120)))))


; Insert Queries
; ==========================================

(deftest insert-queries
  (testing ":insert with a map of cols to vals"
    (let [data {:id 6 :name "Orange Juice" :published 1 :price 5}
          query-map {:table :items :insert data}
          result (exec query-map db)]
      (is result) ; not nil or false
      (is (= (first (exec {:table :items :where {:id 6}} db)) data)))))

(deftest where-queries
  (testing ":where with a map of cols to vals"
    (let [query-map {:table :items
                     :where {:published false}}
          result (exec query-map db)]
      (is (= result (filter #(zero? (:published %)) dummy-data)))
      (is (= (count result) 1))))

  (testing ":where with the use of comparator functions"
    (let [query-map {:table :items
                     :where {:price {:> 100 :< 400 :not= 200}}}
          result (exec query-map db)]
      (is (= result (filter #(and (> (:price %) 100)
                                  (< (:price %) 400)
                                  (not= (:price %) 200)) dummy-data)))
      (is (= (count result) 2))))

  (testing ":where with 'than or equal to' comparators"
    (let [query-map {:table :items
                     :where {:price {:>= 100 :<= 400}}}
          result (exec query-map db)]
      (is (= result (filter #(and (>= (:price %) 100)
                                  (<= (:price %) 400)) dummy-data)))
      (is (= (count result) 2))))

  (testing ":where with the use of a vector of possible values"
    (let [query-map {:table :items :where {:id [1 2 3]}}
          result (exec query-map db)]
      (is (= (count result) 3))))

  (testing "a :where with an empty vector assume NULL."
    (let [query-map {:table :items :where {:id []}}
          result (exec query-map db)]
      (is (= (count result) 0)))))


; Group-By Queries
; ==========================================

(deftest group-queries
  (testing ":group with one column name"
    (let [query-map {:table :items
                     :select [:published '(sum :price)]
                     :group [:published]}
          result (exec query-map db)]
      (is (= (count result) 2))
      (is (= (get-in (first result) [:sum :price]) 405))))

  (testing ":group with multiple column names"
    (let [query-map {:table :items
                     :select [:published '(sum :price)]
                     :group [:price :published]}
          result (exec query-map db)]
      (is (= (count result) 5))
      (is (= (get-in (first result) [:sum :price]) 50)))))


; Update Queries
; ==========================================

(deftest update-queries
  (testing ":update acting upon a single value"
    (let [query-map {:table :items
                     :update {:name "iPhone 6+"}
                     :where {:name "iPhone 4"}}
          result (exec query-map db)]
      (is result) ; not nil or false
      (is (= (count (exec {:table :items :where {:name "iPhone 6+"}} db)) 1))))

  (testing ":update acting upon a multiple values"
    (let [query-map {:table :items
                     :update {:name "iPhone 6+" :price 500}
                     :where {:name "iPhone 4"}}
          result (exec query-map db)]
      (is result) ; not nil or false
      (is (= (count (exec {:table :items :where {:name "iPhone 6+"}} db)) 1)))))


; Delete Queries
; ==========================================

(deftest delete-queries
  (testing ":delete with a boolean and a :where"
    (let [query-map {:table :items :delete true
                     :where {:name "Xbox"}}
          result (exec query-map db)]
      (is result) ; not nil or false
      (is (= (count (exec {:table :items :where {:name "Xbox"}} db)) 0))))

  (testing ":delete with the :all keyword and no :where statement"
    (let [query-map {:table :items :delete :all}
          result (exec query-map db)]
      (is result) ; not nil or false
      (is (= (count (exec {:table :items} db)) 0)))))


; Limit Queries
; ==========================================

(deftest limit-queries
  (testing ":limit with an integer"
    (let [query-map {:table :items :limit 1}
          result (exec query-map db)]
      (is (= (count result) 1)))))


; Order Queries
; ==========================================

(deftest limit-queries
  (testing ":order with a vector containing a col and order"
    (let [query-map {:table :items :order [:price :asc]}
          result (exec query-map db)]
      (is (= (count result) 5))
      (is (= (-> result first :price) 25)))))

; Here we gooo
; ==========================================

(deftest stress-test
  (let [query-map {:table :items
                   :select [:name :id :published '(avg :price)]
                   :where {:price {:>= 20}}
                   :limit 6
                   :order [:id :desc]
                   :group [:published :name]}
        result (exec query-map db)]
    (is (= result [{:avg {:price 199}, :published 0, :id 5, :name "iPhone 4"}
                   {:avg {:price 30}, :published 1, :id 4, :name "Legos"}
                   {:avg {:price 300}, :published 1, :id 3, :name "Xbox"}
                   {:avg {:price 50}, :published 1, :id 2, :name "Play Set"}
                   {:avg {:price 25}, :published 1, :id 1, :name "Rubix Cube"}]))))