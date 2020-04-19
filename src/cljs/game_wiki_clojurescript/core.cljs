(ns game-wiki-clojurescript.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as dom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [re-frame.core :as rf]
   [game-wiki-clojurescript.cards.views :as cards]
   [game-wiki-clojurescript.faqs.views :as faqs]
   [game-wiki-clojurescript.re-frame]
   [game-wiki-clojurescript.routing :as routing]
   [game-wiki-clojurescript.layout :as layout]
   [game-wiki-clojurescript.d3.views :as d3]))

;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (session/get-in [:route :current-page])]
      [:div
       [layout/the-header]
       [:div {:class "main-view container"}
        [page]]
       [layout/the-footer]])))

;; -------------------------
;; Translate routes -> page components
; I tried to put this in the routing file, but it creates
; circular dependencies (it requires the view files, and the
; view files require routing). This could probably be solved with
; some dynamic variables, but that was outside what I wanted to 
; figure out
(defn page-for [route]
  (case route
    :card-list #'cards/cards-list-page
    :faq-list #'faqs/faq-list-page
    :faq-view #'faqs/faq-view-page
    :faq-edit #'faqs/faq-edit-page
    :faq-new #'faqs/faq-edit-page
    :faq-search #'faqs/faq-search-page
    :d3-overview #'d3/overview-page
    :d3-bar-chart #'d3/bar-chart-page
    :d3-bar-chart-v2 #'d3/bar-chart-v2-page
    :d3-line-chart #'d3/line-chart-page
    :d3-line-chart-v2 #'d3/line-chart-v2-page
    :d3-line-chart-v3 #'d3/line-chart-v3-page
    :d3-tetherball-chart #'d3/tetherball-chart-page
    (str "Unknown route " route)))

;; -------------------------
;; Initialize app

(defn mount-root []
  (dom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path routing/router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (r/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :current-route (routing/route-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path routing/router path)))
    :reload-same-path? true})
  (accountant/dispatch-current!)
  (rf/dispatch-sync [:initialize])
  (mount-root))
