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
   [game-wiki-clojurescript.re-frame]))

;; -------------------------
;; Routes
(def router
  (reitit/router
   [["/" :index]
    ["/cards" {:name :card-list :area :cards}]
    ["/faqs" {:area :faqs}
     ["" :faq-list]
     ["/:faq-id" :faq]]]))

(defn route-for [route & [params]]
  (if params
    (reitit/match-by-name router route params)
    (reitit/match-by-name router route)))

(defn path-for [route & [params]]
  (:path (if params
           (route-for route params)
           (route-for route))))

;; -------------------------
;; Page components

;; (defn item-page []
;;   (fn []
;;     (let [routing-data (session/get :route)
;;           item (get-in routing-data [:route-params :item-id])]
;;       [:span.main
;;        [:h1 (str "Item " item " of game-wiki-clojurescript")]
;;        [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))

(defn the-header []
  (fn []
    [:header
     [:nav {:class "navbar navbar-dark bg-primary navbar-expand-lg"}
      [:a {:class "navbar-brand" :href "#"} "Game Wiki"]
      [:button {:class "navbar-toggler" :type "button" :data-toggle "collapse"
                :data-target "#navbar-toggler"}
       [:span {:class "navbar-toggler-icon"}]]
      [:div#navbar-toggler {:class "collapse navbar-collapse"}
       (let [current-area (session/get-in [:route :current-route :data :area])]
         [:ul {:class "navbar-nav"}
         ;; TODO change active based on route
          [:li.nav-item {:class (str (if (= :cards current-area) "active"))}
           [:a.nav-link {:href (path-for :card-list)} "Card List"]]
          [:li.nav-item {:class (str (if (= :faqs current-area) "active"))}
           [:a.nav-link {:href (path-for :faq-list)} "FAQ"]]])]]]))

(defn the-footer []
  (fn []
    [:div.footer
     [:div.container
      [:span "Game Wiki © 2020"]]]))

;; -------------------------
;; Translate routes -> page components
; Should these just be rolled into the router?
(defn page-for [route]
  (case route
    :card-list #'cards/cards-list-page
    :faq-list #'faqs/faq-list-page
    :faq #'faqs/faq-page))

;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [the-header]
       [:div {:class "main-view container"}
        [page]]
       [the-footer]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (dom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (r/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :current-route (route-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (rf/dispatch-sync [:initialize])
  (mount-root))
