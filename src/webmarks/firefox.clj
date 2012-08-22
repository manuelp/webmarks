;; This ns is for importing bookmarks and tags from Firefox.
(ns webmarks.firefox
  (:require [webmarks.mutable :as model])
  (:use [cheshire.core]))

;; Testing purposes
(def tree {"title" ""
           "type" "text/x-moz-place-container"
           "children" [{"title" "git"
                        "type" "text/x-moz-place-container"
                        "children" [{"title" "GitHub"
                                     "type" "text/x-moz-place"
                                     "uri" "http://www.github.com"}]
                        }]
           })

(defn- is-container? [node]
  (= (get node "type") "text/x-moz-place-container"))

(defn- is-bookmark? [node]
  (= (get node "type") "text/x-moz-place"))

(defn visit-node [node tag]
  (let [title (get node "title")]
    (cond (is-bookmark? node) (model/add-tag (get node "uri") tag)
          (is-container? node) (map #(visit-node % title) (get node "children")))))

(defn extract-webmarks [bookmarks]
  (visit-node bookmarks (:title bookmarks)))