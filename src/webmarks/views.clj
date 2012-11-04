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

