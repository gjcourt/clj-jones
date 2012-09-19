(ns clj-jones.core
  (:require [clj-jones.api :as api])
  (:use [clj-jones.util])
  )

(defalias defjones api/defjones)

(defn get-key
  [jones key]
  (let [hmap (get-data jones)]
    (hmap key)))

(defn set-key
  [jones key value]
  (let [hmap (get-data jones)]
    (set-data! jones (assoc hmap key value))))
