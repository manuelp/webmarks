(defproject webmarks "0.2.1"
  :description "Manage bookmarks with tags."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-marginalia "0.7.1"]]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [cheshire "4.0.1"]
                 [ring "1.1.6"]
                 [compojure "1.1.3"]
                 [enlive "1.0.1"]
                 [clj-time "0.4.4"]]
  :main webmarks.web)