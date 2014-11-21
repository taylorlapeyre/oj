![oj](http://i.imgur.com/xEi1K4l.jpg)

# oj

[![Build Status](https://travis-ci.org/taylorlapeyre/oj.svg?branch=master)](https://travis-ci.org/taylorlapeyre/oj)

A refreshing Clojure library for talking to your database, heavily influenced by [Ring][ring].

#### Features
- Gives you a [standard interface](https://github.com/taylorlapeyre/oj/blob/master/doc/SPEC) for generating SQL
- Enforces type checking and validation for queries
- Sensible defaults
- Concise and powerful API
- Encourages reusable components

#### Anti-features
- Doesn't require you to write SQL
- Doesn't create its own domain-specific language (there are no macros)
- Doesn't surprise you


The [SPEC][spec] file provides a complete description of the OJ interface.

## Installation

Add this to your Leiningen :dependencies:

```
[oj "0.1.8"]
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

## Usage

Queries are represented as a Clojure map. The full specification of a **query map** can be found [here](/doc/SPEC).
``` clojure
(def users-named-taylor
  {:table :users
   :select [:id :email]
   :where {:first_name "taylor"}})
```

Queries can be executed by passing a query map and a database config into `oj/exec`:
``` clojure
(def db {:subprotocol "mysql"
         :subname "//127.0.0.1:3306/wishwheel3"
         :user "root"
         :password ""})

(oj/exec users-named-taylor db)
; => ({:id 1 :email "taylorlapeyre@gmail"} ...)
```

**Modifiers** are functions that transform a query map into another query map. This allows us to chain them together. Some basic modifiers are provided by default at `oj.modifiers`.
``` clojure
(require [oj.core :as oj])
         [oj.modifiers :as db])

(defn find-by-username [username]
  (-> (db/query :users)
      (db/select [:id :username :email :created_at])
      (db/where {:username username})
      (oj/exec db-config)
      (first)))

(find-by-username "taylorlapeyre")
; => {:id 1 :username "taylorlapeyre"}
```

OJ's roots in regular Clojure data structures make it extremely powerful for building abstractions.
``` clojure
(defn user [& forms]
  (let [query (reduce merge {:table :users} forms)]
    (oj/exec query db)))

(user {:where {:id 1}})
=> SELECT * FROM users WHERE users.id=1

(user {:where {:id 1}}
      {:select [:id :username]})
=> SELECT id, username FROM users WHERE users.id=1
```

Not quite ActiveRecord, but it's getting there. And in 3 lines of code no less!

Of course, you can also perform all of the standard CRUD operations that you'd expect:
``` clojure
(defn create [user-data]
  (when (valid? user-data)
    (-> (db/query :users)
        (db/insert user-data)
        (oj/exec db-config))))

(defn update [id user-data]
  (when (valid? user-data)
    (-> (db/query :users)
        (db/where {:id id})
        (db/update user-data)
        (oj/exec db-config))))

  (defn delete [id]
    (-> (db/query :users)
        (db/where {:id id})
        (db/delete true)
        (oj/exec db-config)))
  ```

OJ gives you a lot of flexibility. For instance, you could write some custom modifier functions and then execute them when you like. This allows you to combine them.
  ``` clojure
(defn find-by-username
  ([query username]
    (-> query
        (db/where {:username username})))
  ([username]
    (-> (db/query :users)
        (db/where {:username username}))))

; Joins are also easily done.
(-> (find-by-username "taylor")
    (db/join :items)
    (oj/exec db-config)
    (first))
; => {:username "taylor" ... :items ({:id 1 :name "A thing"})}
```

## Printing SQL Queries

If you'd like SQL queries logged to your console when executed, you can enable it by setting the environment variable `PRINT_DB_LOGS` to true.

## Contributing

1. Fork this repository
2. Create a new branch
3. Do your thing
4. Submit a pull request with a description of the change.

## TODO

- Aggregate functions


## License

Copyright © 2014 Taylor Lapeyre

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[yesql]: https://github.com/krisajenkins/yesql
[ring]: https://github.com/ring-clojure/ring
[spec]: /doc/SPEC
