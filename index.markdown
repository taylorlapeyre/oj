---
layout: default
---

{% highlight clojure %}
(require [oj.core :as oj]
         [oj.modifiers :as db])

(defn find-by-username [username]
  (-> (db/query :users)
      (db/select [:id :username :email :created_at])
      (db/where {:username username})
      (oj/exec db-config)
      (first)))
{% endhighlight %}
