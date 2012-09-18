(ns clj-jones.cache
  (:import [com.netflix.curator.framework.recipes.cache PathChildrenCache PathChildrenCacheListener])
  (:use [clj-jones.util :only [deserialize]])
  )

(defn mk-cache
  [jones]
  (let [c (PathChildrenCache. (:zk jones) "/services/storm" true)]
    (.start c)
    c))

(defrecord ChildData [data path stat])

(defmacro add-listener!
  [jones cache & expr]
  `(let [listener# (reify PathChildrenCacheListener
                    (childEvent [this# client# event#]
                      (let [cdata# (.getData event#)
                            ~'event {:node (ChildData. (deserialize (.getData cdata#))
                                                       (.getPath cdata#)
                                                       (.getStat cdata#))
                                     :type (.getType event#)}]
                        (if (= (:conf ~jones) (-> ~'event :node :path))
                          ~@expr))))]
     (-> ~cache .getListenable (.addListener listener#))))

