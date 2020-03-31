(ns game-wiki-clojurescript.faqs.views
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [re-frame.core :as rf]
            [game-wiki-clojurescript.faqs.re-frame]
            [game-wiki-clojurescript.routing :as routing]))

;; Helper funcs 
; Gets value of js event (for use in :on-change, etc.)
(def get-event-value #(-> % .-target .-value))

(defn search []
  (let [search-term (session/get-in [:route :route-params :search-term])
        val (r/atom search-term)]
    (fn []
      [:form.form-inline
       [:div
        [:input.mr-4.form-control {:placeholder "search" :default-value @val
                                   :on-change #(reset! val (get-event-value %))}]]
       [:button.btn.btn-primary {:type "button"
                                 :on-click #(routing/navigate! :faq-search {:search-term @val})}
        "Search"]])))

(defn popular-tags []
  [:div.inline.small
   [:span.label "Popular Tags: "]
   (for [tag @(rf/subscribe [:faq-popular-tags])]
     ^{:key tag} [:a.ml-2 {:href (routing/path-for :faq-search {:search-term tag})} tag])])

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
