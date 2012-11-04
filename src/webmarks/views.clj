(ns webmarks.views
  (:use [net.cgrand.enlive-html :as h])
  (:require [clj-time.core :as dt]))

(h/defsnippet footer "footer.html" [:div] []
  [:span.year] (h/content (str (dt/year (dt/now)))))

(h/defsnippet sidebar "sidebar.html" [:div] [])

(h/deftemplate layout "layout.html" [title]
  [:#title] (h/content title)
  [:.sidebar] (h/content (sidebar))
  [:.footer] (h/content (footer)))

(h/defsnippet tags-list "tags-list.html" [:div] [tags]
  [:a] (h/clone-for [tag tags]
                     (h/do->
                      (h/set-attr :href (str "/search/by-tag/" tag))
                      (h/content tag)
                      (h/after " "))))

(h/deftemplate tags-page "layout.html" [title tags]
  [:#title] (h/content title)
  [:.sidebar] (h/content (sidebar))
  [:.content] (h/content (tags-list tags))
  [:.footer] (h/content (footer)))

(h/defsnippet webmark-entry "webmarks-list.html" [:span.entry] [webmark]
  [:a.webmark] (h/do->
                (h/set-attr :href (first webmark))
                (h/content (first webmark)))
  [:a.tag-link] (h/clone-for [tag (second webmark)]
                             (h/do->
                              (h/set-attr :href (str "/search/by-tag/" tag))
                              (h/content tag)
                              (h/after ","))))

(h/defsnippet webmarks-list "webmarks-list.html" [:div] [webmarks]
  [:li] (h/clone-for [webmark webmarks]
                     (h/content (webmark-entry webmark))))

(h/deftemplate webmarks-page "layout.html" [title tags]
  [:#title] (h/content title)
  [:.sidebar] (h/content (sidebar))
  [:.content] (h/content (webmarks-list tags))
  [:.footer] (h/content (footer)))
