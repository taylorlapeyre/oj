![oj](http://i.imgur.com/xEi1K4l.jpg)

# oj

[![Build Status](https://travis-ci.org/taylorlapeyre/oj.svg?branch=master)](https://travis-ci.org/taylorlapeyre/oj)

A refreshing Clojure library for executing SQL, heavily influenced by [Ring][ring].

OJ gets out of your way and lets you focus on writing good, reusable code.

#### Features
- [Standard interface](/docs/spec) for generating SQL
- Concise and powerful API
- Sensible defaults
- Everything is *just Clojure*

#### Anti-features
- Doesn't require you to write SQL
- Doesn't create its own domain-specific language (there are no macros)
- Doesn't surprise you


The [SPEC][spec] file provides a complete description of the OJ interface.

## Installation

Add this to your Leiningen :dependencies:

```
[oj "0.1.2"]
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

Queries are represented as a Clojure map. The full specification of a query map can be found [here](/doc/spec).
``` clojure
(def users-named-taylor
  {:table :users
   :select [:id :email]
   :where {:first_name "taylor"}})

(oj/exec users-named-taylor db-config)
; => ({:id 1 :email "taylorlapeyre@gmail"} ...)
```

Modifiers are functions that transform a query map into another query map. This allows us to chain them together.
``` clojure
(require [oj.core :as oj]
         [oj.modifiers :as sql]
         [myapp.config :refer [db-config]))

(defn find-by-username [username]
  (-> (sql/query :users)
      (sql/select [:id :username :email :created_at])
      (sql/where {:username username})
      (oj/exec db-config)
      (first)))

(find-by-username "taylorlapeyre")
; => {:id 1 :username "taylorlapeyre"}
```

You can also perform all of the standard CRUD operations that you'd expect.
``` clojure
(defn create [user-data]
  (when (valid? user-data)
    (-> (sql/query :users)
        (sql/insert user-data)
        (oj/exec db-config))))

(defn update [id user-data]
  (when (valid? user-data)
    (-> (sql/query :users)
        (sql/where {:id id})
        (sql/update user-data)
        (oj/exec db-config))))
  
  (defn delete [id]
    (-> (sql/query :users)
        (sql/where {:id id})
        (sql/delete true)
        (oj/exec db-config)))
  ```
  
OJ gives you a lot of flexibility. For instance, you could write some custom modifer functions and then execute them when you like. This allows you to combine them.
  ``` clojure
(defn find-by-username
  ([query username]
    (-> query
        (sql/where {:username username})))
  ([username]
    (-> (sql/query :users)
        (sql/where {:username username}))))

; Joins are also easily done.
(-> (find-by-username "taylor")
    (sql/join :items)
    (oj/exec db-config)
    (first))
; => {:username "taylor" ... :items ({:id 1 :name "A thing"})}
```

## Contributing

1. Fork this repository
2. Create a new branch
3. Do your thing
4. Submit a pull request with a description of the change.

## TODO

- [ ] Other comparators besides `=` in `:where`
- [ ] Aggregate functions


## License

Copyright © 2014 Taylor Lapeyre

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[yesql]: https://github.com/krisajenkins/yesql
[ring]: https://github.com/ring-clojure/ring
[spec]: /doc/SPEC
