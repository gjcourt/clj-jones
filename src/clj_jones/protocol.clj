(ns clj-jones.protocol
  (:import [com.netflix.curator.framework CuratorFrameworkFactory])
  (:import [com.netflix.curator.retry RetryNTimes])
  (:require [clojure.contrib.string :as string])
  (:use [clj-jones.util])
  )

(defprotocol IJones
  (zk [this])
  )

(defrecord Jones [root conf]
  IJones
  (zk [this] (:zk this))
  )

(defn mk-jones
  [hosts ports service]
  (let [root-path (mk-root-path service)
        conf-path (mk-conf-path service)
        hostports (string/join "," (map #(string/join ":" %&)
                                        hosts
                                        ports))
        policy (RetryNTimes. (int 3) (int 50))
        zk-client (CuratorFrameworkFactory/newClient hostports policy)
        jones (->Jones root-path conf-path)]
    (.start zk-client)
    (assoc jones :zk zk-client)
    ))
