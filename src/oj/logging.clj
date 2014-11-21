(ns oj.logging)

(defn highlight [word]
  (str "\033[91m" word "\033[0m"))

(defn highlight-word [word]
  (if (re-matches #"[A-Z]+" word)
    (highlight word)
    word))

(defn pretty-log [query]
  (let [words (clojure.string/split query #" ")]
    (->> (map highlight-word words)
         (interpose " ")
         (apply str)
         (println))))