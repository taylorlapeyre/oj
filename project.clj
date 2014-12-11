(defproject oj "0.2.4"
  :description "A refreshing way to talk to your database."
  :url "http://github.com/taylorlapeyre/oj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.clojure/tools.logging "0.3.1"]]

  :profiles {:dev {:dependencies [[com.h2database/h2 "1.4.182"]]}})
