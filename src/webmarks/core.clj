(ns webmarks.core
  (:require [clojure.set :as cset]
            [clojure.string :as str]))

;; Functional model
;; ================
;; This is the heart of the application: pure functions to manipulate
;; data.

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
  (let [webmark (first (filter-by-url webmarks url))]
    (assoc webmarks (key webmark) (conj (val webmark) new-tag))))

(defn rm-tag
  "Returns new webmarks map without the tag-to-rm associated with the given url."
  [webmarks url tag-to-rm]
  (let [webmark (first (filter-by-url webmarks url))]
    (assoc webmarks (key webmark) (disj (val webmark) tag-to-rm))))

;; Mutable model
;; =============
;; This model build up the functional one to provide a way to
;; manipulate a persistent data structure containing the webmarks.

;; The persistent data structure that represents state is hold by an
;; atom (uncoordinated synchronized concurrency), that will be
;; manipulated in memory and eventually persisted.
(def webmarks (atom {}))

(defn- split-tags [tags-str]
  (str/split tags-str #","))

(defn add-webmark [url tags]
  (swap! webmarks assoc url (apply hash-set (split-tags tags))))

(defn add-tag [url new-tag]
  (swap! webmarks add-new-tag url new-tag))

(defn remove-tag [url new-tag]
  (swap! webmarks rm-tag url new-tag))