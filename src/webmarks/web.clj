(ns webmarks.web
  (:use compojure.core)
  (:require (compojure [handler :as handler]
                       [route :as route])
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            (webmarks [views :as view]
                      [mutable :as mutable]
                      [persistence :as persistence])
            [ring.adapter.jetty :as jetty]
            (ring.util [response :as response]
                       [codec :as rc])
            (clj-time [format :as tformat]
                      [core :as tcore])
            [clojure.pprint :as pp])
  (:gen-class))

(def users {"manuel" {:username "manuel"
                      :password (creds/hash-bcrypt (or (System/getenv "PASSWORD")
                                                       "password"))
                      :roles #{::user}}})

(def containers (atom []))

(defn- init-containers [edn-filename db-spec]
  (do
    (swap! containers conj
           (persistence/->PostgresDatabase db-spec 100)
           (persistence/->ClojureFile edn-filename))))

(defroutes app*
  (route/resources "/")
  (GET "/" [] (friend/authorize #{::user}
                                (view/layout "WebMarks!")))
  (GET "/login" [] (view/login-page "WebMarks - Login"))

  (GET "/list" [] (friend/authorize #{::user}
                                    (view/webmarks-page "WebMarks - List"
                                                        @mutable/webmarks)))
  (GET "/tags" [] (friend/authorize #{::user}
                                    (view/tags-page "WebMarks - Tags"
                                                    (sort (mutable/tags-list)))))
  (GET "/search/by-tag/:tag" [tag]
       (friend/authorize #{::user}
                         (view/webmarks-page (str "WebMarks - Tag: " tag)
                                             (mutable/filter-by-tags [tag]))))
  (GET "/add" [] (friend/authorize #{::user}
                                   (view/add-webmark "WebMarks - Add New")))
  (POST "/add" [url tags] (friend/authorize #{::user}
                                            (do
                                              (mutable/add-new-webmark (rc/url-decode url)
                                                                       tags)
                                              (mutable/save-webmarks! @containers)
                                              (response/redirect-after-post "/"))))
  (GET "/edit/:encoded-url"
       [encoded-url]
       (friend/authorize #{::user}
                         (let [url (rc/url-decode encoded-url)]
                           (view/edit-webmark "WebMarks - Edit" url
                                              (get @mutable/webmarks url)))))
  (POST "/edit/:encoded-url" [encoded-url new-tag & checked]
        (friend/authorize #{::user}
                          (let [url (rc/url-decode encoded-url)
                                tags-to-remove (vals checked)]
                            (doall (map (partial mutable/remove-tag url) tags-to-remove))
                            (if new-tag (mutable/add-tag url new-tag))
                            (mutable/save-webmarks! @containers)
                            (response/redirect-after-post
                             (str "/edit/" (rc/url-encode url))))))
  (GET "/delete/:encoded-url"
       [encoded-url]
       (friend/authorize #{::user}
                         (let [url (rc/url-decode encoded-url)]
                           (mutable/remove-webmark url)
                           (mutable/save-webmarks! @containers)
                           (response/redirect "/"))))

  (GET "/export" []
       (friend/authorize #{::user}
                         (let [today (tformat/unparse
                                      (:date-hour-minute tformat/formatters)
                                      (tcore/now))
                               filename (str "webmarks-" today ".edn")]
                           (->
                            (response/response (with-out-str
                                                 (pp/pprint @mutable/webmarks)))
                            (response/header "Content-Disposition"
                                             (str "attachment; filename=" filename))
                            (response/content-type "text/plain")))))
  
  (friend/logout (ANY "/logout" request (response/redirect "/")))
  (route/not-found "Not Found"))

;; The Ring handler generated by Compojure is enriched by the
;; Compojure's `site` middleware (a composition of Ring middlewares,
;; see
;; [documentation](http://weavejester.github.com/compojure/compojure.handler.html#var-api))
;; and bound to a var.
(def app
  (handler/site
   (friend/authenticate app*
   			{:credential-fn (partial creds/bcrypt-credential-fn users)
                         :workflows [(workflows/interactive-form)]})))

(defn- ensure-edn-file [filename]
  (let [file (clojure.java.io/file filename)]
    (if (.createNewFile file)
      (spit filename "{}"))))

;; This way, we can start the embedded Jetty server and dynamically
;; change our handler rebinding `app` at will without having to
;; restart the server.
(defn -main [& args]
  (let [port (or (and (first args)
                      (Integer/parseInt (first args)))
                 (and (System/getenv "PORT")
                      (Integer/parseInt (System/getenv "PORT")))
                 8080)
        edn-filename (or (second args)
                         (System/getenv "WEBMARKS_FILE")
                         "webmarks.edn")]
    (do
      (ensure-edn-file edn-filename)
      (init-containers edn-filename (System/getenv "DATABASE_URL"))
      (mutable/load-webmarks! @containers)
      (jetty/run-jetty #'app {:port port :join? false}))))