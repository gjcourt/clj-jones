Clojure library for [jones](https://github.com/disqus/jones)
============================================================

Usage
-----
    => (use 'clj-jones.core)
    => (defjones jones ["example" "localhost" 2181])
    => (get-key jones "foo")
    => "bar"
    => (set-key jones "foo" "bar2")
    => #<Stat 29,542,1347744967483,1348084140676,76,0,0,0,38,0,29>
    => (get-key "foo")
    => "bar2"

You can also implement callbacks for a specific service by simple defining an expression. It's implemented
with an anaphoric macro, so you'll have access to the `event` object in the expression scope. There is an
implicit do so feel free to have multiple expressions.

    => (defjones jones ["example" "localhost" 2181]
    =>   (println "EVENT TRIGGERED" event))
    => (get-key jones "foo")
    => "bar2"
    => (set-key jones "foo" "buzz")
    => #<Stat 29,542,1347744967483,1348084140676,76,0,0,0,38,0,29>
    => TRIGGERED {:node #clj_jones.api.ChildData{:data {bar one, foo buzz, one two}, :path /services/storm/conf, :stat #<Stat 29,542,1347744967483,1348084140676,76,0,0,0,38,0,29>}, :type #<Type CHILD_UPDATED>}

Distributed under the Eclipse Public License, the same as Clojure.
