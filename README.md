Clojure library for [jones](https://github.com/disqus/jones)
============================================================

    -> (use 'clj-jones.core)
    -> (def c (client ["localhost"] [2181] "service name"))
    -> (get c "foo")
    -> "bar"
    -> (set c "foo" "bar2")
    -> (get c "foo")
    -> "bar2"

Distributed under the Eclipse Public License, the same as Clojure.
