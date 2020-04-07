(ns game-wiki-clojurescript.d3.views
  (:require [reagent.core :as r]
            [game-wiki-clojurescript.d3.bar-chart :as bar]
            [game-wiki-clojurescript.d3.line-chart :as line]
            [game-wiki-clojurescript.d3.tetherball-chart :as tetherball]
            [game-wiki-clojurescript.routing :as routing]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Usage
(def dataset
  [{:label "A" :value 10}
   {:label "B" :value 20}
   {:label "C" :value 30}
   {:label "D" :value 50}])

(def bar-chart {:title "My Chart"
                :width 200
                :height 200
                :bar-colors ["red" "green" "blue"]})

(defn bar-chart-page []
  (let [ratom (r/atom {:dataset dataset
                       :chart bar-chart})]
    [:div
     [:h3 "Bar Chart Demo"]
     [bar/bar-chart {:ratom ratom}]]))

(def line-chart {:title "My Chart"
                 :width 200
                 :height 200
                 :line-color "#1274a3"})

(defn line-chart-page []
  (let [ratom (r/atom {:dataset dataset
                       :chart line-chart})]
    [:div
     [:h3 "Line Chart Demo"]
     [line/line-chart {:ratom ratom}]]))

(def tetherball-chart-dataset-1 {:min 200
                                 :max 250
                                 :value 240
                                 :target 230})
(def tetherball-chart-dataset-2 {:min 200
                                 :max 250
                                 :value 220
                                 :target 230})
(def tetherball-chart-dataset-3 {:min 220
                                 :max 270
                                 :value 240
                                 :target 230})
(def tetherball-chart-1 {:title "My Tetherball Chart"
                         :width 500
                         :height 100
                         :id "tetherball-graph-1"})
(def tetherball-chart-2 {:title "My Tetherball Chart"
                         :width 500
                         :height 100
                         :id "tetherball-graph-2"})
(def tetherball-chart-3 {:title "My Tetherball Chart"
                         :width 500
                         :height 100
                         :id "tetherball-graph-3"})

(defn tetherball-chart-page []
  [:div
   [:h3 "Tetherball Demo"]
   (let [ratom (r/atom {:dataset tetherball-chart-dataset-1
                        :chart tetherball-chart-1})]
     [:div
      [:span "Min 200 | Target 230 | Value 240 | Max 250"]
      [tetherball/tetherball-chart {:ratom ratom}]])
   (let [ratom (r/atom {:dataset tetherball-chart-dataset-2
                        :chart tetherball-chart-2})]
     [:div
      [:span "Min 200 | Value 220 | Target 230 | Max 250"]
      [tetherball/tetherball-chart {:ratom ratom}]])
   (let [ratom (r/atom {:dataset tetherball-chart-dataset-3
                        :chart tetherball-chart-3})]
     [:div
      [:span "Min 220 | Target 230 | Value 240 | Max 270"]
      [tetherball/tetherball-chart {:ratom ratom}]])])

(defn overview-page []
  [:div
   [:h2 "Examples"]
   [:ul
    [:li
     [:a {:href (routing/path-for :d3-bar-chart)} "Bar Chart"]]
    [:li
     [:a {:href (routing/path-for :d3-line-chart)} "Line Chart"]]
    [:li
     [:a {:href (routing/path-for :d3-tetherball-chart)} "Tetherball Chart"]]]])
