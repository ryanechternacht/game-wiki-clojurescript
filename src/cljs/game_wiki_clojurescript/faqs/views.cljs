(ns game-wiki-clojurescript.faqs.views
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [re-frame.core :as rf]
            [game-wiki-clojurescript.faqs.re-frame]
            [game-wiki-clojurescript.routing :as routing]))

(defn search []
  (fn []
    [:form.form-inline
     [:input.mr-4.form-control {:placeholder "search"}]
     ;; TODO search
     [:button.btn.btn-primary {:type "button"} "Search"]]))

(defn popular-tags []
  [:div.inline.small
   [:span.label "Popular Tags: "]
   (for [tag @(rf/subscribe [:faq-popular-tags])]
       ;;TODO routing
     ^{:key tag} [:a.ml-2 {:href "#"} tag])])

; TODO figure out how to make this use child routes so the
; header isn't constantly being redrawn
(defn header []
  [:div
   [:div.float-right
      ;; TODO hookup
    [:button.btn.btn-outline-primary "New Entry"]]
   [search]
   [:div.mt-2
    [popular-tags]]])

(defn faq-list-page []
  [:div
   [header]
   [:hr]
   [:div "Hello World - I'm the FAQ page"]])

;; (defn item-page []
;;   (fn []
;;     (let [routing-data (session/get :route)
;;           item (get-in routing-data [:route-params :item-id])]
;;       [:span.main
;;        [:h1 (str "Item " item " of game-wiki-clojurescript")]
;;        [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))


(defn faq-page []
  [:div
   [header]
   [:hr]
   [:div "Hello World - I'm 1 faq"]])

(defn faq-search-page []
  [:div
   [header]
   [:hr]
   [:h2 (str "Results for " @(rf/subscribe [:faq-search-term]))]
   [:ol
    (for [{:keys [id title]} @(rf/subscribe [:faq-search-results])]
      ^{:key id}
      [:li
       [:a {:href (routing/path-for :faq {:faq-id id})} title]])]])
