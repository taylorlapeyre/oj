(ns oj.logging
  "Functions for logging SQL queries to stdout."
  (:require [clojure.tools.logging :as log]))

(defn pretty-log [query]
  (when (System/getenv "PRINT_DB_LOGS")
    (log/info query)))
