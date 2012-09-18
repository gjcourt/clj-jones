(ns clj-jones.core
  (:require [clj-jones.protocol :as protocol])
  (:require [clj-jones.cache :as jcache])
  (:use [clj-jones.util])
  )

(defn mk-jones
  [hosts ports service]
  (protocol/mk-jones hosts ports service))

(defn mk-cache
  [jones]
  (jcache/mk-cache jones))

(def e (atom nil))
; NOTE testing
(defonce jones (mk-jones ["localhost"] [2181] "storm"))
(defonce cache (mk-cache jones))
(jcache/add-listener! jones cache (reset! e event))
(println "Num listeners" (.size (.getListenable cache)))

(defn get
  [jones key]
  (let [hmap (get-data jones)]
    (hmap key)))

(defn set
  [jones key value]
  (let [hmap (get-data jones)]
    (set-data! jones (assoc hmap key value))))
