(ns webmarks.mutable
  (:use clojure.pprint)
  (:require [webmarks.core :as core]
            [webmarks.persistence :as persistence]
            [clojure.string :as s])
  (:gen-class))

;; Mutable model
;; =============
;; This model build up the functional one to provide a way to
;; manipulate a persistent data structure containing the webmarks.

;; The persistent data structure that represents state is hold by an
;; atom (uncoordinated synchronized concurrency), that will be
;; manipulated in memory and eventually persisted.
(def webmarks (atom {}))

(defn- split-tags [tags-str]
  (s/split tags-str #","))

(defn add-new-webmark
  "Create/update a webmark with the given comma-separated list of
  tags."
  [url tags]
  (do 
    (swap! webmarks core/add-webmark url (apply hash-set (split-tags tags)))))

(defn remove-webmark [url]
  (swap! webmarks core/rm-webmark url))

(defn add-tag
  "Assign a tag to a URL. If there isn't any webmark for that URL,
  it'll be created."
  [url new-tag]
  (swap! webmarks core/add-new-tag url new-tag))

(defn remove-tag [url new-tag]
  (swap! webmarks core/rm-tag url new-tag))

(defn filter-by-tags [tags]
  (apply core/filter-by-tags (cons @webmarks tags)))

(defn filter-by-url [url-re]
  (core/filter-by-url @webmarks url-re))

(defn tags-list []
  (core/tags-list @webmarks))

;; TODO Prompt mini-library
(def commands {"tags" ["Tags list"
                       (fn [& words]
                         (pprint (tags-list)))]
               "by-tag" ["Search by tag"
                         (fn [& tags]
                           (pprint (filter-by-tags tags)))]
               "by-url" ["Search by URL regexp"
                         (fn [re]
                           (pprint (filter-by-url re)))]
               })

(defn- read-command [prompt]
  (do (print prompt)
      (flush)
      (s/split (read-line) #"\s")))

;; TODO Help auto-construction and print (help command or unknown)
;; TODO Collision with existing commands (help)?

(defn- get-cmd-fn [commands cmd-str]
  (let [cmd-entry (get commands cmd-str)
        default-fn #(println "Unknown command")]
    (if (nil? cmd-entry)
      default-fn
      (second cmd-entry))))

(defn- run-command [commands command args]
  (let [cmd-fn (get-cmd-fn commands command)]
    (apply cmd-fn (vec args))))

(defn- command-loop [prompt quit-cmd commands-map]
  (loop []
    (let [cmd (read-command prompt)]
     (if (= quit-cmd (first cmd))
       (println "Bye!")
       (do
         (run-command commands-map (first cmd) (rest cmd))
         (recur))))))

(defn load-webmarks!
  "Load webmarks into the atom from the given file."
  [filename]
  (reset! webmarks (.load-data (persistence/->ClojureFile filename))))

(defn save-webmarks!
  [filename]
  (.save-data (persistence/->ClojureFile filename) @webmarks))

(defn -main
  "Start a CLI to interact with webmarks loaded from the given file."
  [edn-filename & args]
  (let [file (or edn-filename "webmarks.edn")]
    (load-webmarks! file)
    (command-loop "[> " "quit" commands)))