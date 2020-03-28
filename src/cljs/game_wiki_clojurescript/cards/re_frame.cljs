(ns game-wiki-clojurescript.cards.re-frame
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

;; Helper funcs
(def next-filter-id (r/atom 0))
(defn get-next-filter-id []
  (do
    (swap! next-filter-id inc)))

;; takes a filter and returns a func to be used with filter
;; category filter looks like {:category {:tag "type", :value "automated"}}
(defn make-filter-fn [{:keys [category has does-not-have]}]
  (cond
    category (let [{tag :tag cat-value :value} category]
               (fn [{tags :tags}]
                 (some (fn [{name :name tag-value :value}]
                         (and (= name tag) (= cat-value tag-value)))
                       tags)))
    has (let [{target :tag} has]
          (fn [{tags :tags}]
            (some (fn [{name :name}]
                    (= name target))
                  tags)))
    does-not-have (let [{target :tag} does-not-have]
                    (fn [{tags :tags}]
                      (not-any? (fn [{name :name}]
                                  (= name target))
                                tags)))
    :else (fn [x] true)))

;; Events
; TODO prevent duplicates
(rf/reg-event-db
 :add-card-filter
 [(rf/path [:cards :active-filters])]
 (fn [active-filters [_ filter]]
   (conj active-filters (assoc filter :id (get-next-filter-id)))))

(rf/reg-event-db
 :clear-filters
 [(rf/path [:cards :active-filters])]
 (fn [_ _] []))

(rf/reg-event-db
 :remove-filter
 [(rf/path [:cards :active-filters])]
 (fn [filters [_ id]]
   (vec (filter #(not= (:id %) id) filters))))

;; Subscriptions
(rf/reg-sub
 :cards
 (fn [db _]
   (get-in db [:cards :card-list])))

(rf/reg-sub
 :cards-active-filters
 (fn [db _]
   (get-in db [:cards :active-filters])))

(rf/reg-sub
 :cards-filters-types
 (fn [db _]
   (get-in db [:cards :filters :type])))

(rf/reg-sub
 :cards-filters-tags
 (fn [db _]
   (get-in db [:cards :filters :building-tags])))

(rf/reg-sub
 :cards-filters-action
 (fn [db _]
   (get-in db [:cards :filters :action])))

(rf/reg-sub
 :visible-cards
 (fn [query-v _]
   [(rf/subscribe [:cards])
    (rf/subscribe [:cards-active-filters])])
 (fn [[cards filters] _]
   (if (empty? filters)
     cards
     (filter (apply every-pred (map make-filter-fn filters)) cards))))
