(ns clj-jones.core
  (:require [clj-jones.api :as api])
  (:use [clj-jones.util])
  )

(defalias defjones api/defjones)

(defn get
  [jones key]
  (let [hmap (get-data jones)]
    (hmap key)))

(defn set
  [jones key value]
  (let [hmap (get-data jones)]
    (set-data! jones (assoc hmap key value))))

; NOTE test
; (def e (atom nil))
; ; (defjones jones1 ["storm" "localhost" 2181])
; (defjones jones2 ["storm" "localhost" 2181]
;   (reset! e event)
;   (println "TRIGGERED"))
