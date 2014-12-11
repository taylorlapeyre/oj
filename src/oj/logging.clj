(ns oj.logging)

(defn highlight [sentence]
  (str "\033[91m" sentence "\033[0m"))

(defn pretty-log [query]
  (when (System/getenv "PRINT_DB_LOGS")
    (println (highlight query))))
