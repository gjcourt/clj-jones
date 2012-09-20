(ns clj-jones.core
  (:import [com.netflix.curator.framework CuratorFrameworkFactory])
  (:import [com.netflix.curator.retry RetryNTimes])
  (:import [com.netflix.curator.framework.recipes.cache PathChildrenCache PathChildrenCacheListener])
  (:require [clojure.contrib.string :as string])
  (:require [clj-jones.api :as api])
  (:use [clj-jones.util])
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
  (zk [this])
  (cache [this])
  (path [this])
  (get-key [this key])
  (set-key [this key value])
  )

(defrecord Jones [parent conf]
  IJones
  (zk [this]
    (when-not @(:cache this)
      @(:zk this)
      (reset! (:cache this) (mk-parent-cache @(:zk this)
                                              (:parent this)))
      (when-let [handler (:listener this)]
        (-> @(:cache this) .getListenable (.addListener handler))))
    @(:zk this))
  (cache [this]
    (when-not @(:cache this)
      @(:zk this)
      (reset! (:cache this) (mk-parent-cache @(:zk this)
                                              (:parent this)))
      (when-let [handler (:listener this)]
        (-> @(:cache this) .getListenable (.addListener handler))))
    @(:cache this))
  (path [this] (:conf this))
  (get-key [this key]
    (let [hmap (get-data (zk this) (:conf this))]
      (hmap key)))
  (set-key [this key value]
    (let [hmap (get-data (zk this) (:conf this))]
      (set-data! (zk this) (:conf this) (assoc hmap key value))))
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
         curator-client# (delay (mk-curator-client hostports# policy#))
         parent-cache# (delay (mk-parent-cache curator-client# parent-path#))
         jones# (->Jones parent-path# conf-path#)
         listener# (if-not ~(nil? expr) (reify PathChildrenCacheListener
                                          (childEvent [this# client# event#]
                                            (let [cdata# (.getData event#)
                                                  ~'event {:node (->ChildData (deserialize (.getData cdata#))
                                                                              (.getPath cdata#)
                                                                              (.getStat cdata#))
                                                           :type (.getType event#)}]
                                              (when (= (-> ~'event :node :path) conf-path#)
                                                ~@expr)))))]
     (def ~name (assoc jones#
                       :zk curator-client#
                       :cache (atom nil)
                       :listener listener#))
     ))
