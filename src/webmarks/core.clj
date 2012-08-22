(ns webmarks.core
  (:require [clojure.set :as cset]))

;; Functional model
;; ================
;; This is the heart of the application: pure functions to manipulate
;; data.

(defn add-webmark [webmarks url tags]
  (assoc webmarks url tags))

(defn rm-webmark [webmarks url]
  (dissoc webmarks url))

(defn filter-by-tags
  "Filter only webmarks associated with *all* given tags."
  [webmarks & tags]
  (filter #(cset/subset? (set tags) (val %)) webmarks))

(defn filter-by-url
  "Filter webmarks by regexp on the url."
  [webmarks url-re]
  (let [pattern (re-pattern url-re)
        matches? (fn [str]
                   (not (nil? (re-seq pattern str))))]
    (filter #(matches? (key %)) webmarks)))

;; This function is useful for producing suggestions.
(defn tags-list
  "Returns a set with all tags ever used."
  [webmarks]
  (apply cset/union (map val webmarks)))

(defn add-new-tag
  "Returns new webmarks map with the new-tag associated with the given url."
  [webmarks url new-tag]
  (let [webmark (first (filter #(= url (key %)) webmarks))]
    (if (nil? webmark)
      (add-webmark webmarks url #{new-tag})
      (assoc webmarks (key webmark) (conj (val webmark) new-tag)))))

(defn rm-tag
  "Returns new webmarks map without the tag-to-rm associated with the given url."
  [webmarks url tag-to-rm]
  (let [webmark (first (filter-by-url webmarks url))]
    (assoc webmarks (key webmark) (disj (val webmark) tag-to-rm))))