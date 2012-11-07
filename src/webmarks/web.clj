(ns webmarks.web
  (:use [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :only (redirect-after-post)]
        [compojure.core :only (defroutes GET POST)]
        [ring.util.codec :only (url-decode)]
        clojure.pprint)
  (:require (compojure route handler)
            [webmarks.mutable :as mutable]
            [webmarks.views :as view]))

(def webmarks-filename (atom ""))

(defroutes routes*
  (compojure.route/resources "/")
  (GET "/" [] (view/layout "WebMarks!"))
  (GET "/list" [] (view/webmarks-page "WebMarks - List" @mutable/webmarks))
  (GET "/tags" [] (view/tags-page "WebMarks - Tags" (mutable/tags-list)))
  (GET "/search/by-tag/:tag" [tag]
       (view/webmarks-page (str "WebMarks - Tag: " tag)
                           (mutable/filter-by-tags [tag])))
  (GET "/add" [] (view/add-webmark "WebMarks - Add New"))
  (POST "/add" [url tags] (do
                            (mutable/add-new-webmark url tags)
                            (mutable/save-webmarks! @webmarks-filename)
                            (redirect-after-post "/")))
  (GET "/edit/:encoded-url" [encoded-url]
       (let [url (url-decode encoded-url)]
         (view/edit-webmark "WebMarks - Edit" url (get @mutable/webmarks url))))
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
      (reset! webmarks-filename edn-filename)
      (run-jetty #'routes {:port port :join? false}))))