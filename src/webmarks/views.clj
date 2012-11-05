(ns webmarks.views
  (:use [net.cgrand.enlive-html :as h]
        clojure.pprint)
  (:require [clj-time.core :as dt]
            [ring.util.codec :as rc]))

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
  [:a.webmark-edit] (h/set-attr :href (str "/edit/" (rc/url-encode (first webmark))))
  [:a.tag-link] (let [tags (second webmark)
                      num-tags (count tags)
                      tags-pairs (partition 2 (interleave tags (range 1 (inc num-tags))))]
                  (h/clone-for [[tag idx] tags-pairs]
                               (h/do->
                                (h/set-attr :href (str "/search/by-tag/" tag))
                                (h/content tag)
                                (h/after (if (< idx num-tags) "," ""))))))

(h/defsnippet webmarks-list "webmarks-list.html" [:div] [webmarks]
  [:li] (h/clone-for [webmark webmarks]
                     (h/content (webmark-entry webmark))))

(h/deftemplate webmarks-page "layout.html" [title tags]
  [:#title] (h/content title)
  [:.sidebar] (h/content (sidebar))
  [:.content] (h/content (webmarks-list tags))
  [:.footer] (h/content (footer)))

(h/defsnippet new-webmark-form "new-webmark-form.html" [:form] [])

(h/deftemplate add-webmark "layout.html" [title]
  [:#title] (h/content title)
  [:.sidebar] (h/content (sidebar))
  [:.content] (h/content (new-webmark-form))
  [:.footer] (h/content (footer)))

(h/defsnippet edit-webmark-form "edit-webmark.html" [:div.edit-webmark] [url tags]
  [:p#webmark-url] (h/content url)
  [:.tags :span.webmark-tag] (h/clone-for [tag tags]
                                          (h/content tag)))

(h/deftemplate edit-webmark "layout.html" [title url tags]
  [:#title] (h/content title)
  [:.sidebar] (h/content (sidebar))
  [:.content] (h/content (edit-webmark-form url tags))
  [:.footer] (h/content (footer)))