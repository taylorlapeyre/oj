(ns oj.logging
  (:require [environ.core :refer [env]]))

(defn highlight [sentence]
  (str "\033[91m" sentence "\033[0m"))

(defn pretty-log [query]
  (when (env :print-db-logs)
    (println (highlight query))))
