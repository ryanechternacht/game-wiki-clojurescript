(ns game-wiki-clojurescript.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as dom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/items"
     ["" :items]
     ["/:item-id" :item]]
    ["/about" :about]
    ["/cards" :cards-list]
    ["/faq" :faq-list]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

;; (defn home-page []
;;   (fn []
;;     [:span.main
;;      [:h1 "Welcome to game-wiki-clojurescript"]
;;      [:ul
;;       [:li [:a {:href (path-for :items)} "Items of game-wiki-clojurescript"]]
;;       [:li [:a {:href "/broken/link"} "Broken link"]]]]))

;; (defn items-page []
;;   (fn []
;;     [:span.main
;;      [:h1 "The items of game-wiki-clojurescript"]
;;      [:ul (map (fn [item-id]
;;                  [:li {:name (str "item-" item-id) :key (str "item-" item-id)}
;;                   [:a {:href (path-for :item {:item-id item-id})} "Item: " item-id]])
;;                (range 1 60))]]))

;; (defn item-page []
;;   (fn []
;;     (let [routing-data (session/get :route)
;;           item (get-in routing-data [:route-params :item-id])]
;;       [:span.main
;;        [:h1 (str "Item " item " of game-wiki-clojurescript")]
;;        [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))

(defn about-page []
  (fn [] [:span.main
          [:h1 "About game-wiki-clojurescript"]]))

(defn the-header []
  (fn []
    [:header
     [:nav {:class "navbar navbar-dark bg-primary navbar-expand-lg"}
      [:a {:class "navbar-brand" :href "#"} "Game Wiki"]
      [:button {:class "navbar-toggler" :type "button" :data-toggle "collapse"
                :data-target "#navbar-toggler"}
       [:span {:class "navbar-toggler-icon"}]]
      [:div#navbar-toggler {:class "collapse navbar-collapse"}
       [:ul {:class "navbar-nav"}
         ;; TODO Links
        [:li {:class "nav-item active"}
         [:a {:class "nav-link" :href "/cards"} "Card List"]]
        [:li {:class "nav-item"}
         [:a {:class "nav-link" :href "/faq"} "FAQ"]]]]]]))

(defn the-footer []
  (fn []
    [:div.footer
     [:div.container
      [:span "Game Wiki © 2020"]]]))

(defn cards-list-page []
  (fn []
    [:div "Hello World - I'm the cards page"]))

(defn faq-list-page []
  (fn []
    [:div "Hello World - I'm the FAQ page"]))

;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    ;; :index #'home-page
    ;; :about #'about-page
    ;; :items #'items-page
    ;; :item #'item-page
    :cards-list #'cards-list-page
    :faq-list #'faq-list-page))

;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [the-header]
       [page]
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
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
