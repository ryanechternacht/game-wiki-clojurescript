(ns game-wiki-clojurescript.faqs.re-frame
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]))

;; Events
(rf/reg-event-db
 :save-faq
 :<- [:faqs]
 (fn [faqs [_ faq]]
   (assoc faqs (:id faq) faq)))

;; Subscriptions
(rf/reg-sub
 :faqs
 (fn [db _]
   (vals (get-in db [:faqs :faq-list]))))

(rf/reg-sub
 :faq-popular-tags
 :<- [:faqs]
 (fn [faqs _]
   (->> faqs
        (mapcat (fn [m] (:tags m)))
        (reduce #(assoc %1 %2 (inc (get %1 %2 0))) {})
        (filter #(> (second %) 1))
        (sort-by second >)
        (take 4)
        (map first))))

(rf/reg-sub
 :faq-search-results
 :<- [:faqs]
 (fn [faqs [_ search-term]]
   (->> faqs
        (filter #(or (str/includes? (:title % "") search-term)
                     (str/includes? (:body % "") search-term)
                     (some (fn [t] (str/includes? t search-term)) (:tags % []))))
        (map #(select-keys % [:title :id])))))

(rf/reg-sub
 :faq
 :<- [:faqs]
 (fn [faqs [_ id]]
   (->> faqs
        (filter #(= (:id %) id))
        first)))
