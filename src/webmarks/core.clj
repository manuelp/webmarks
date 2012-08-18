(ns webmarks.core
  (:require [clojure.set :as cset]
            [clojure.string :as str]))

;; Functional model
;; ================

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

;; Mutable model
;; =============

(def webmarks (atom {}))

(defn- split-tags [tags-str]
  (str/split tags-str #","))

(defn add-webmark [url tags]
  (swap! webmarks assoc url (apply hash-set (split-tags tags))))