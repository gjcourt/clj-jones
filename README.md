Clojure library for [Jones](https://github.com/disqus/jones)

-> (use 'clj-jones.core)
-> (def c (client ["localhost"] [2181] "service name"))
-> (get c "foo")
-> "bar"

Distributed under the Eclipse Public License, the same as Clojure.
