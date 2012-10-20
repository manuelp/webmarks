;; This ns is for importing bookmarks and tags from Firefox.
(ns webmarks.firefox
  (:require [webmarks.mutable :as model]
            [clojure.string :as s])
  (:use cheshire.core
        clojure.pprint)
  (:gen-class))

;; For testing:
(def tree {"title" ""
           "type" "text/x-moz-place-container"
           "children" [{"title" "scm"
                        "type" "text/x-moz-place-container"
                        "children" [{"title" "GitHub"
                                     "type" "text/x-moz-place"
                                     "uri" "http://www.github.com"}
                                    {"title" "BitBucket"
                                     "type" "text/x-moz-place"
                                     "uri" "http://www.bitcubket.org"}]
                        }
                       {"title" "languages"
                        "type" "text/x-moz-place-container"
                        "children" [{"title" "Clojure"
                                     "type" "text/x-moz-place"
                                     "uri" "http://www.clojure.org"}
                                    {"title" "Haskell"
                                     "type" "text/x-moz-place"
                                     "uri" "http://www.haskell.org"}]
                        }]
           })

(defn- is-container? [node]
  (= (get node "type") "text/x-moz-place-container"))

(defn- is-bookmark? [node]
  (= (get node "type") "text/x-moz-place"))

(defn- get-children [node]
  (get node "children"))

(defn- is-leaf? [node]
  (= nil (get-children node)))

(defn- title [node]
  (get node "title"))

(defn- url [node]
  (get node "uri"))

(defn- build-webmark [bookmark parent]
  (do
    (println (title parent) "->" (url bookmark))
    (model/add-tag (url bookmark) (title parent))))

(defn- tag-cloud [node]
  (let [children (get-children node)]
    (s/join " " (map #(format "%s(%d)" (title %) (count (get-children %))) children))))

(defn- gather-webmarks [node parent]
  (if (is-leaf? node)
    (build-webmark node parent)
    (doall (map #(gather-webmarks % node) (get-children node)))))

(defn- find-tags [node]
  (if (= "Tags" (title node))
    (vector node)
    (mapcat find-tags (get-children node))))

(defn import-json [filename]
  (let [json (parse-string (slurp filename))
        tags (nth (find-tags json) 3)]
    (gather-webmarks tags nil)))

(defn -main []
  (import-json "bookmarks.json")
  (spit "output.json" (with-out-str (pprint @model/webmarks)))
  (println (format "# webmarks importati: %d" (count @model/webmarks))))