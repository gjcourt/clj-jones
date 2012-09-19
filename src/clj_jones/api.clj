(ns clj-jones.api
  (:import [com.netflix.curator.framework CuratorFrameworkFactory])
  (:import [com.netflix.curator.retry RetryNTimes])
  (:import [com.netflix.curator.framework.recipes.cache PathChildrenCache PathChildrenCacheListener])
  (:require [clojure.contrib.string :as string])
  (:use [clj-jones.util :only [deserialize
                               maybe-vector
                               mk-parent-path
                               mk-conf-path]])
  )

(defn mk-curator-retry-policy
  [number & [timeout]]
  (RetryNTimes. (int number)
                (int (or timeout 50))))

(defn mk-curator-client
  [hostports policy]
  (let [client (CuratorFrameworkFactory/newClient hostports policy)]
    (.start client)
    client))

(defn mk-parent-cache
  [curator-client parent-path]
  (let [c (PathChildrenCache. curator-client parent-path true)]
    (.start c)
    c))

(defprotocol IJones
  (zk [this] :zk this)
  (cache [this] :cache this)
  )

(defrecord Jones [parent conf]
  IJones
  )

(defrecord ChildData [data path stat])

(defmacro defjones
  [name [service hosts ports] & expr]
  `(let [parent-path# (mk-parent-path ~service)
         conf-path# (mk-conf-path ~service)
         hosts-vec# (maybe-vector ~hosts)
         ports-vec# (maybe-vector ~ports)
         hostports# (string/join "," (map #(string/join ":" %&)
                                          hosts-vec#
                                          ports-vec#))
         policy# (mk-curator-retry-policy 3)
         curator-client# (mk-curator-client hostports# policy#)
         parent-cache# (mk-parent-cache curator-client# parent-path#)
         jones# (->Jones parent-path# conf-path#)]
     (if-not ~(nil? expr)
       (-> parent-cache# .getListenable (.addListener
                                          (reify PathChildrenCacheListener
                                            (childEvent [this# client# event#]
                                              (let [cdata# (.getData event#)
                                                    ~'event {:node (->ChildData (deserialize (.getData cdata#))
                                                                                (.getPath cdata#)
                                                                                (.getStat cdata#))
                                                             :type (.getType event#)}]
                                                (when (= (-> ~'event :node :path) conf-path#)
                                                  ~@expr))))))
       )
     (def ~name (assoc jones# :zk curator-client# :cache parent-cache#))
     ))
