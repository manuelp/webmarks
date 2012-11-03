(ns webmarks.web
  (:use [ring.adapter.jetty :only (run-jetty)]
        [compojure.core :only (defroutes GET)])
  (:require (compojure route handler)
            [webmarks.mutable :as mutable]))

(defroutes routes*
  (GET "/list" [] (with-out-str
                    (pprint @mutable/webmarks)))
  (GET "/tags" [] (with-out-str
                    (pprint (mutable/tags-list))))
  (GET "/search/by-tag/:tag" [tag] (with-out-str
                                     (pprint (mutable/filter-by-tags [tag]))))
  (GET "/search/by-url/:url" [url] (with-out-str
                                     (pprint (mutable/filter-by-url url))))
  (compojure.route/not-found "Sorry, there is nothing here."))

(def routes (compojure.handler/api routes*))

(defn -main [& args]
  (let [port (or (Integer/parseInt (first args)) 8080)
        edn-filename (or (second args) "webmarks.edn")]
    (do
      (mutable/load-webmarks! edn-filename)
      (run-jetty #'routes {:port port :join? false}))))