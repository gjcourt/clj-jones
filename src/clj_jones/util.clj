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
  (if-not (vector? s) (vector s) s))

(defn serialize [data]
  (utf8-byte-array (json/encode data)))

(defn deserialize [data]
  (json/decode (apply str (map char data))))

(defn zk-get
  [curator path]
  (let [builder (.getData curator)]
    (-> builder (.forPath path))))

(defn zk-set!
  [curator path data]
  (let [builder (.setData curator)]
    (-> builder (.forPath path data))))

(defn zk-create!
  [curator path data]
  (let [builder (.create curator)]
    (-> builder .creatingParentsIfNeeded (.forPath path data))
    ))

(defn set-data!
  [curator path data]
  (let [sdata (serialize data)]
    (try
      (zk-set! curator path sdata)
      (catch Exception ex
        (zk-create! curator path (serialize {}))
        (zk-set! curator path sdata))
      )))

(defn get-data
  [curator path]
  (try
    (deserialize (zk-get curator path))
    (catch Exception ex
      {}
      )))

(defn- del
  [curator path key]
  (let [hmap (get-data path curator)
        builder (.setData curator)]
    (-> builder
      (.forPath path
                (utf8-byte-array
                  (json/encode (dissoc hmap key)))))
    ))
