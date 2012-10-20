(ns webmarks.mutable
  (:require [webmarks.core :as core]
            [clojure.string :as str]))

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

(defn add-new-webmark
  "Create/update a webmark with the given comma-separated list of
  tags."
  [url tags]
  (swap! webmarks core/add-webmark url (apply hash-set (split-tags tags))))

(defn remove-webmark [url]
  (swap! webmarks core/rm-webmark url))

(defn add-tag
  "Assign a tag to a URL. If there isn't any webmark for that URL,
  it'll be created."
  [url new-tag]
  (swap! webmarks core/add-new-tag url new-tag))

(defn remove-tag [url new-tag]
  (swap! webmarks core/rm-tag url new-tag))