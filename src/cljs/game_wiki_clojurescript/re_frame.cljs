(ns game-wiki-clojurescript.re-frame
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

;; Events
(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:cards {:card-list [{:name "colonizer training camp"
                         :id 1
                         :tags [{:name "building tag" :value "jove"}
                                {:name "building tag" :value "building"}
                                {:name "type" :value "automated"}
                                {:name "vps" :value 2}]
                         :cost 8}
                        {:name "asteroid mining consortium"
                         :id 2
                         :tags [{:name "building tag" :value "jove"}
                                {:name "vps" :value 1}
                                {:name "type" :value "automated"}]
                         :cost 13}
                        {:name "deep well heating"
                         :id 3
                         :tags [{:name "building tag" :value "power"}
                                {:name "building tag" :value "building"}
                                {:name "type" :value "automated"}]
                         :cost 13}
                        {:name "cloud seeding"
                         :id 4
                         :tags [{:name "type" :value "automated"}]
                         :cost 11}
                        {:name "search for life"
                         :id 5
                         :tags [{:name "building tag" :value "science"}
                                {:name "type" :value "active"}
                                {:name "action" :value true}]
                         :cost 3}
                        {:name "inventors guild"
                         :id 6
                         :tags [{:name "building tag" :value "science"}
                                {:name "action" :value "true"}
                                {:name "type" :value "active"}]
                         :cost 9}
                        {:name "martian rails"
                         :id 7
                         :tags [{:name "building tag" :value "building"}
                                {:name "action" :value "true"}
                                {:name "type" :value "active"}]
                         :cost 13}
                        {:name "capital"
                         :id 8
                         :tags [{:name "building tag" :value "city"}
                                {:name "building tag" :value "building"}
                                {:name "type" :value "automated"}]
                         :cost 26}
                        {:name "asteroid"
                         :id 9
                         :tags [{:name "building tag" :value "event"}
                                {:name "building tag" :value "space"}
                                {:name "type" :value "event"}]
                         :cost 14}
                        {:name "comet"
                         :id 10
                         :tags [{:name "building tag" :value "event"}
                                {:name "building tag" :value "space"}
                                {:name "type" :value "event"}]
                         :cost 21}
                        {:name "big asteroid"
                         :id 11
                         :tags [{:name "building tag" :value "event"}
                                {:name "building tag" :value "space"}
                                {:name "type" :value "event"}]
                         :cost 27}
                        {:name "water import from europa"
                         :id 12
                         :tags [{:name "building tag" :value "jove"}
                                {:name "type" :value "active"}
                                {:name "action" :value true}
                                {:name "vps" :value "*"}]
                         :cost 27}
                        {:name
                         "release of inert gases"
                         :id 36
                         :tags [{:name "building tag" :value "event"}
                                {:name "raises TR" :value 2}
                                {:name "type" :value "event"}]
                         :cost 17}]
            :active-filters []
            :filters {:type {:category "type"
                             :values ["automated" "active" "event"]
                             :description "Card Type"}
                      :building-tags {:category "building tag"
                                      :values ["power"
                                               "building"
                                               "city"
                                               "event"
                                               "space"
                                               "jove"
                                               "venus"
                                               "animal"
                                               "microbe"
                                               "plant"
                                               "science"]
                                      :description "Building Tag"}}}}))

;;TODO add an inc id to filter (for reagent keys)
(rf/reg-event-db
 :add-card-filter
 [(rf/path [:cards :active-filters])]
 (fn [active-filters [_ filter]]
   (conj active-filters filter)))

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

;; takes a filter and returns a func to be used with filter
;; category filter looks like {:category {:tag "type", :value "automated"}}
(defn make-filter-fn [{:keys [category]}]
  (cond
    category (let [{tag :tag cat-value :value} category]
               (fn [{tags :tags}]
                 (some (fn [{name :name tag-value :value}]
                         (and (= name tag) (= cat-value tag-value)))
                       tags)))
    :else (fn [x] true)))

(rf/reg-sub
 :visible-cards
 (fn [query-v _]
   [(rf/subscribe [:cards])
    (rf/subscribe [:cards-active-filters])])
 (fn [[cards filters] _]
   (if (empty? filters)
     cards
     (filter (apply every-pred (map make-filter-fn filters)) cards))))
