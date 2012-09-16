(ns clj-jones.core
  (:import [com.netflix.curator.framework CuratorFrameworkFactory])
  (:import [com.netflix.curator.retry RetryUntilElapsed])
  (:require [clojure.contrib.io :as io])
  (:require [clojure.contrib.string :as string])
  (:require [cheshire.core :as json])
  )

(defn utf8-byte-array
  [string]
  (io/to-byte-array string))

(defn client
  [hosts ports service]
  (let [hostports (string/join "," (map #(string/join ":" %&)
                                        hosts
                                        ports))
        policy (RetryUntilElapsed. (int 500) (int 50))
        client (CuratorFrameworkFactory/newClient hostports policy)]
    (.start client)
    {:zk client
     :service service}
    ))

(defn mk-path
  [service]
  (str "/services/" service "/conf"))

(defn- get-hmap
  [client]
  (let [builder (.getData (:zk client))
        path (mk-path (:service client))]
    (json/decode
      (apply str (map char (-> builder (.forPath path)))))
    ))

(defn get
  [client key]
  (let [hmap (get-hmap client)]
    (hmap key)
    ))

(defn set
  [client key value]
  (let [hmap (get-hmap client)
        builder (.setData (:zk client))
        path (mk-path (:service client))]
    (-> builder
      (.forPath path
                (utf8-byte-array
                  (json/encode (assoc hmap key value)))))
    ))

(defn create
  [client key value]
  (let [hmap (get-hmap client)
        builder (.create (:zk client))
        path (mk-path (:service client))]
    (-> builder
      (.creatingParentsIfNeeded
        (.forPath path
                  (utf8-byte-array
                    (json/encode (assoc hmap key value))))))
    ))

(defn del
  [client key]
  (let [hmap (get-hmap client)
        builder (.setData (:zk client))
        path (mk-path (:service client))]
    (-> builder
      (.forPath path
                (utf8-byte-array
                  (json/encode (dissoc hmap key)))))
    ))
