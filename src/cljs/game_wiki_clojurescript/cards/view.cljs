(ns game-wiki-clojurescript.cards.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [game-wiki-clojurescript.cards.re-frame]))

;; Helper funcs 
; Gets value of js event (for use in :on-change, etc.)
(def get-event-value #(-> % .-target .-value))

; Determines the text that should appear in the active filter chip
(defn determine-filter-text [{:keys [category has does-not-have] :as filter}]
  (cond
    category (str (:tag category) ": " (:value category))
    has (str "Has " (:tag has))
    does-not-have (str "Doesn't Have " (:tag does-not-have))
    :else (str filter)))

;; Components
(defn active-filters []
  (fn []
    [:div {:class "header-row inline chip-height"}
     "Active Filters: "
     [:div.inline
      (for [filter @(rf/subscribe [:cards-active-filters])]
        (let [id (:id filter)]
          ^{:key id} [:span.chip (determine-filter-text filter)
                      [:span.close-button
                       {:on-click #(rf/dispatch [:remove-filter id])} "X"]]))]]))

(defn category-filter [{:keys [category values description]}]
  (let [val (r/atom (first values))]
    (fn []
      [:form {:class "form-inline"}
       [:label {:class "mr-4"} (str description ":")]
       [:select {:class "mr-4 custom-select"
                 :on-change #(reset! val (get-event-value %))}
        (for [v values]
          [:option {:key (keyword v)} v])]
       [:button
        {:type "button"
         :class "btn btn-outline-primary"
         :on-click #(rf/dispatch
                     [:add-card-filter {:category {:tag category
                                                   :value @val}}])}
        "Add Filter"]])))

(defn existence-filter [{:keys [description type]}]
  (fn []
    [:form {:class "form-inline"}
     [:label {:class "mr-4"} description]
     [:div.btn-group
      [:button {:type "button" :class "btn btn-outline-primary"
                :on-click #(rf/dispatch
                            [:add-card-filter {:has {:tag type}}])} "Yes"]
      [:button {:type "button" :class "btn btn-outline-primary"
                :on-click #(rf/dispatch
                            [:add-card-filter {:does-not-have {:tag type}}])} "No"]]]))

(defn clear-filters []
  (fn []
    [:div {:class "header-row"}
     [:button {:class "btn btn-primary"
               :on-click #(rf/dispatch [:clear-filters])} "Clear Filters"]]))

(defn cards-filtering []
  (fn []
    [:div
     [:h3 "Filter Cards"]
     [active-filters]
     [:div {:class "header-row"}
      [category-filter @(rf/subscribe [:cards-filters-types])]]
     [:div {:class "header-row"}
      [category-filter @(rf/subscribe [:cards-filters-tags])]]
     [:div {:class "header-row"}
      [existence-filter @(rf/subscribe [:cards-filters-action])]]
     [clear-filters]]))

;; View
(defn cards-list-page []
  (fn []
    [:div
     [cards-filtering]
     [:hr]
     (let [cards @(rf/subscribe [:visible-cards])]
       [:div
        [:h4 "Cards: " (count cards)]
        [:ul
         (for [{:keys [id name]} cards]
           ^{:key id} [:li name])]])]))
