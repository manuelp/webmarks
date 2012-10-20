;; This ns is for importing bookmarks and tags from Firefox.
(ns webmarks.firefox
  (:require [webmarks.mutable :as model]
            [clojure.string :as s])
  (:use cheshire.core
        clojure.pprint)
  (:gen-class))

;; Firefox can export its bookmarks in JSON format. Unfortunately,
;; this format is not so beautiful: it's a tree that contains the same
;; bookmark multiple times.
;;
;; Every bookmark can be contained in a *folder* and can have multiple
;; *tags*. In the JSON exported by Firefox, each folder and tag is a
;; node of the tree, so a bookmark can appear in multiple subtrees.

;; This is a small example of a *tags subtree* of an hypothetical JSON
;; exported by Firefox.
(def tree-example {"title" "Tags"
                   "type" "text/x-moz-place-container"
                   "children" [{"title" "scm"
                                "type" "text/x-moz-place-container"
                                "children" [{"title" "GitHub"
                                             "type" "text/x-moz-place"
                                             "uri" "http://www.github.com"}
                                            {"title" "BitBucket"
                                             "type" "text/x-moz-place"
                                             "uri" "http://www.bitbucket.org"}]
                                }
                               {"title" "languages"
                                "type" "text/x-moz-place-container"
                                "children" [{"title" "Clojure"
                                             "type" "text/x-moz-place"
                                             "uri" "http://www.clojure.org"}
                                            {"title" "Haskell"
                                             "type" "text/x-moz-place"
                                             "uri" "http://www.haskell.org"}]
                                }
                               {"title" "simple"
                                "type" "text/x-moz-place-container"
                                "children" [{"title" "Clojure"
                                             "type" "text/x-moz-place"
                                             "uri" "http://www.clojure.org"}]
                                }]
                   })

;; ## Inspection ##
;; The following functions are useful to get informations out of the
;; JSON tree nodes.

(defn- is-container? [node]
  (= (get node "type") "text/x-moz-place-container"))

(defn- is-bookmark? [node]
  (= (get node "type") "text/x-moz-place"))

(defn- title [node]
  (get node "title"))

(defn- url [node]
  (get node "uri"))

(defn- get-children
  "Get all the children nodes of the given one. If the current node is
  a leaf, it returns `nil`."
  [node]
  (get node "children"))

(defn- is-leaf? [node]
  (= nil (get-children node)))

;; ## JSON tree visiting and webmarks generation ##

(defn- build-webmark
  "Build a webmark from the input bookmark node."
  [bookmark parent]
  (do
    (println (title parent) "->" (url bookmark))
    (model/add-tag (url bookmark) (title parent))))

(defn- tag-cloud
  "This function can be applied to a tags subtree to see what tags it
  contains and how many bookmarks there are for every one of them."
  [node]
  (let [children (get-children node)]
    (s/join " " (map #(format "%s(%d)" (title %) (count (get-children %))) children))))

(defn- gather-webmarks
  "Generate webmarks for every leaf (bookmark) in the input tree.
  Parents are useful in that they represent tags." [node parent]
  (if (is-leaf? node)
    (build-webmark node parent)
    (doall (map #(gather-webmarks % node) (get-children node)))))

(defn- find-tags
  "Find the subtrees that contains tags specifications for the bookmarks."
  [node]
  (if (= "Tags" (title node))
    (vector node)
    (mapcat find-tags (get-children node))))

(defn import-json
  "Import all bookmarks contained in `filename` into the atom in `mutable` ns."
  [filename]
  (let [json (parse-string (slurp filename))
        tags (find-tags json)]
    (doall (map #(gather-webmarks % nil) tags))))

(defn -main
  "Convert the Firefox's bookmarks and tags exported in JSON format to an [edn](https://github.com/edn-format/edn)
format file.

There can be two command line parameters:

- Input JSON filename (default: *bookmarks.json*)
- Output edn filename (default: *webmarks.edn*)"
  [& args]
  (let [json-file (or (first args) "bookmarks.json")
        out-file (or (second args) "webmarks.end")]
    (import-json json-file)
    (spit out-file (with-out-str (pprint @model/webmarks)))
    (println (format "# webmarks importati: %d" (count @model/webmarks)))))