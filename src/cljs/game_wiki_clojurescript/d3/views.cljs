(ns game-wiki-clojurescript.d3.views
  (:require [reagent.core :as r]
            [game-wiki-clojurescript.d3.bar-chart :as bar]
            [game-wiki-clojurescript.d3.line-chart :as line]
            [game-wiki-clojurescript.routing :as routing]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Usage
(def dataset
  [{:label "A" :value 10}
   {:label "B" :value 20}
   {:label "C" :value 30}
   {:label "D" :value 50}])

(def chart {:title "My Chart"
            :width 200
            :height 200
            :bar-colors ["red" "green" "blue"]
            :line-color "#1274a3"})

(defn bar-chart-page []
  (let [ratom (r/atom {:dataset dataset
                       :chart chart})]
    [:div
     [:h3 "Bar Chart Demo"]
     [bar/bar-chart {:ratom ratom}]]))

(defn line-chart-page []
  (let [ratom (r/atom {:dataset dataset
                       :chart chart})]
    [:div
     [:h3 "Line Chart Demo"]
     [line/line-chart {:ratom ratom}]]))

(defn overview-page []
  [:div
   [:h2 "Examples"]
   [:ul
    [:li
     [:a {:href (routing/path-for :d3-bar-chart)} "Bar Chart"]]
    [:li
     [:a {:href (routing/path-for :d3-line-chart)} "Line Chart"]]]])
