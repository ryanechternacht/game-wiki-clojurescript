(ns game-wiki-clojurescript.re-frame
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

;; Events
(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:cards [{:name "hello" :id 1} {:name "world" :id 2}]}))

;; Subscriptions
(rf/reg-sub
 :cards
 (fn [db _]
   (:cards db)))
