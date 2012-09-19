(ns clj-jones.util
  (:require [cheshire.core :as json])
  (:require [clojure.contrib.io :as io])
  )

; from twitter storm
(defmacro defalias
  "Defines an alias for a var: a new var with the same root binding (if
  any) and similar metadata. The metadata of the alias is its initial
  metadata (as provided by def) merged into the metadata of the original."
  ([name orig]
   `(do
      (alter-meta!
        (if (.hasRoot (var ~orig))
          (def ~name (.getRawRoot (var ~orig)))
          (def ~name))
        ;; When copying metadata, disregard {:macro false}.
        ;; Workaround for http://www.assembla.com/spaces/clojure/tickets/273
        #(conj (dissoc % :macro)
               (apply dissoc (meta (var ~orig)) (remove #{:macro} (keys %)))))
      (var ~name)))
  ([name orig doc]
   (list `defalias (with-meta name (assoc (meta name) :doc doc)) orig)))

(defn utf8-byte-array
  [string]
  (io/to-byte-array string))

(defn mk-parent-path
  [service]
  (str "/services/" service))

(defn mk-conf-path
  [service]
  (str (mk-parent-path service) "/conf"))

(defn maybe-vector [s]
  (if-not (seq? s) (vector s) s))

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

