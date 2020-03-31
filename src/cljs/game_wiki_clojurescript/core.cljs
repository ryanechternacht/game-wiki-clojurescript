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
   [game-wiki-clojurescript.layout :as layout]))

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
        (session/put! :route {:current-page (routing/page-for current-page)
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
