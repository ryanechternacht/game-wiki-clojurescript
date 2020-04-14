(ns game-wiki-clojurescript.layout
  (:require
   [reagent.session :as session]
   [game-wiki-clojurescript.routing :as routing]))

(defn the-header []
  [:header
   [:nav {:class "navbar navbar-dark bg-primary navbar-expand-lg"}
    [:a {:class "navbar-brand" :href "#"} "Game Wiki"]
    [:button {:class "navbar-toggler" :type "button" :data-toggle "collapse"
              :data-target "#navbar-toggler"}
     [:span {:class "navbar-toggler-icon"}]]
    [:div#navbar-toggler {:class "collapse navbar-collapse"}
     (let [current-area (session/get-in [:route :current-route :data :area])]
       [:ul {:class "navbar-nav"}
        [:li.nav-item {:class (str (if (= :cards current-area) "active"))}
         [:a.nav-link {:href (routing/path-for :card-list)} "Card List"]]
        [:li.nav-item {:class (str (if (= :faqs current-area) "active"))}
         [:a.nav-link {:href (routing/path-for :faq-list)} "FAQ"]]
        [:li.nav-item {:class (str (if (= :d3 current-area) "active"))}
         [:a.nav-link {:href (routing/path-for :d3-overview)} "d3"]]])]]])

(defn the-footer []
  [:div.footer
   [:div.container
    [:span "Game Wiki © 2020"]]])
