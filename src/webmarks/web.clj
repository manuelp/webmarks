(ns webmarks.web
  (:use [ring.adapter.jetty :only (run-jetty)]
        [compojure.core :only (defroutes GET)]
        clojure.pprint)
  (:require (compojure route handler)
            [webmarks.mutable :as mutable]
            [webmarks.views :as view]))

(defroutes routes*
  (compojure.route/resources "/")
  (GET "/" [] (view/layout "WebMarks!"))
  (GET "/list" [] (with-out-str
                    (pprint @mutable/webmarks)))
  (GET "/tags" [] (with-out-str
                    (pprint (mutable/tags-list))))
  (GET "/search/by-tag/:tag" [tag] (with-out-str
                                     (pprint (mutable/filter-by-tags [tag]))))
  (GET "/search/by-url/:url" [url] (with-out-str
                                     (pprint (mutable/filter-by-url url))))
  (compojure.route/not-found "Sorry, there is nothing here."))

;; The Ring handler generated by Compojure is enriched by the
;; Compojure's `site` middleware (a composition of Ring middlewares,
;; see
;; [documentation](http://weavejester.github.com/compojure/compojure.handler.html#var-api))
;; and bound to a var.
(def routes (compojure.handler/site routes*))

;; This way, we can start the embedded Jetty server and dynamically
;; change our handler rebinding `routes` at will without having to
;; restart the server.
(defn -main [& args]
  (let [port (or (Integer/parseInt (first args)) 8080)
        edn-filename (or (second args) "webmarks.edn")]
    (do
      (mutable/load-webmarks! edn-filename)
      (run-jetty #'routes {:port port :join? false}))))