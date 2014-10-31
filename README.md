# oj

A Clojure library for executing SQL, heavily influenced by [Ring][ring].

OJ helps you interact with your database in a succinct and intuitive way by abstracting SQL queries into standard Clojure data structures. There is no magic here, only data.

The [SPEC][spec] file provides a complete description of the OJ interface.

## Installation

Add this to your Leiningen :dependencies:

```
[oj "0.1.0"]
```

You'll also need a database driver (thanks to [yesql][yesql] for providing
this handy dandy table):

|Database|`:dependencies` Entry|
|---|---|
|PostgreSQL|`[org.postgresql/postgresql "9.3-1102-jdbc41"]`|
|MySQL|`[mysql/mysql-connector-java "5.1.32"]`|
|Oracle|`[com.oracle/ojdbc14 "10.2.0.4.0"]`|
|SQLite|`[org.xerial/sqlite-jdbc "3.7.2"]`|
|Derby|`[org.apache.derby/derby "10.11.1.1"]`|

## Philosophy

OJ provides a low-level Clojure interface over SQL. With it, you can easily create methods that perform queries to do whatever you wish.

OJ tries to do one thing well: transform a Clojure map into an SQL query. With a common foundation,

## Usage

OJ provides a small collection of [modifiers][spec] that you can use to build your queries. You could also simply construct the [query map][spec] yourself.

``` clojure
(ns myapp.models.user
  (:require [oj.core :as oj]
            [oj.modifiers :as sql]))

(->
  (sql/query :users)
  (sql/select [:id :email])
  (sql/where {:id 1}))

; => {:table :users
;     :select [:id :email]
;     :where {:id 1}}
```

After defining a database configuration, you can execute queries with `oj/exec`:

``` clojure
(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/exampledb"
               :user "root"
               :password ""})

(defn find-by-id [id]
  (->
    (sql/query :users)
    (sql/select [:id :])
    (oj/exec mysql-db)
    (first)))

(find-by-id 1)
; => {:id 1 :username "taylor"}
```

## Contributing

1. Fork this repository
2. Create a new branch
3. Do your thing
4. Submit a pull request with a description of the change.

## TODO

- [ ] Joins
- [ ] Tests


## License

Copyright © 2014 Taylor Lapeyre

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[yesql]: https://github.com/krisajenkins/yesql
[ring]: https://github.com/ring-clojure/ring
[spec]: /doc/SPEC
