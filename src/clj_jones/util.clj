(ns clj-jones.util
  (:require [cheshire.core :as json])
  (:require [clojure.contrib.io :as io])
  )

(defn utf8-byte-array
  [string]
  (io/to-byte-array string))

(defn mk-root-path
  [service]
  (str "/services/" service))

(defn mk-conf-path
  [service]
  (str (mk-root-path service) "/conf"))

; (defmacro ghmap
;   [jones & [handler]]
;   (let [watch-handler-wrapper
;         (if handler
;           (fn [builder] ((memfn usingWatcher) builder handler))
;           (fn [builder] builder))]
;     `(let [builder# (.getData (:zk ~jones))
;            path# (:conf ~jones)]
;        (json/decode
;          (apply str (map char (-> builder#
;                                 ~watch-handler-wrapper
;                                 (.forPath path#))))))
;     ))


(defn serialize [data]
  (utf8-byte-array (json/encode data)))

(defn deserialize [data]
  (json/decode (apply str (map char data))))

(defn zk-get
  [jones]
  (let [builder (.getData (:zk jones))
        path (:conf jones)]
    (-> builder (.forPath path))))

(defn zk-set!
  [jones data]
  (let [builder (.setData (:zk jones))
        path (:conf jones)]
    (-> builder
      (.forPath path data))))

(defn zk-create!
  [jones data]
  (let [builder (.create (:zk jones))
        path (:conf jones)]
    (-> builder
      (.creatingParentsIfNeeded
        (.forPath path data)))
    ))

(defn get-data
  [jones]
  (deserialize (zk-get jones)))

(defn set-data!
  [jones data]
  (zk-set! jones (serialize data)))

(defn- del
  [jones key]
  (let [hmap (get-data jones)
        builder (.setData (:zk jones))
        path (:conf jones)]
    (-> builder
      (.forPath path
                (utf8-byte-array
                  (json/encode (dissoc hmap key)))))
    ))

