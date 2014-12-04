(ns oj.modifiers-test
  (:require [clojure.test :refer :all]
            [oj.modifiers :refer :all]))

(deftest update-query-map
  (is (= (update {} {:foo "bar"}) {:update {:foo "bar"}})))

(deftest delete-query-map
  (is (= (delete {}) {:delete true})))
