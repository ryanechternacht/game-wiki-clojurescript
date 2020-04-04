(ns game-wiki-clojurescript.re-frame
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [clojure.string :as str]))

;; initial data
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
                                      :description "Building Tag"}
                      :action {:description "Is an Action"
                               :type "action"}}}
    :faqs {:faq-list {1 {:id 1
                         :title "How many actions can I take in a generation?"
                         :body "<p>As many as you want. You keep taking turns until you pass a turn without taking any actions.</p>"
                         :tags ["general"]}
                      2 {:id 2
                         :title "What are the best expansions?"
                         :body "<ol><li>Prelude</li><li>Colonies</li><li>Turmoil</li><li>Venus</li></ol>"
                         :tags ["general"]}
                      3 {:id 3
                         :title "Is a chairman part of a party?"
                         :body "<p>Who can know?</p>"
                         :tags ["turmoil" "chairman" "party"]}
                      4 {:id 4
                         :title "Can I use a <?> tag during a new government bonus"
                         :body "<p>No. You can only use <?> tags while taking an action</p>"
                         :tags ["turmoil" "?-tag" "general"]}}
           :search-term ""
           :search-results [{:id 1 :title "hello"}
                            {:id 2 :title "world"}]}}))
