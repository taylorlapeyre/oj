---
layout: default
---

{% highlight clojure %}
(require [oj.core :as oj]
         [oj.modifiers :as sql])

(defn find-by-username [username]
  (-> (sql/query :users)
      (sql/select [:id :username :email :created_at])
      (sql/where {:username username})
      (oj/exec db-config)
{% endhighlight %}
