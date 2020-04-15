(ns game-wiki-clojurescript.d3.views
  (:require [reagent.core :as r]
            [game-wiki-clojurescript.d3.bar-chart :as bar]
            [game-wiki-clojurescript.d3.line-chart :as line]
            [game-wiki-clojurescript.d3.line-chart-v2 :as line-v2]
            [game-wiki-clojurescript.d3.tetherball-chart :as tetherball]
            [game-wiki-clojurescript.routing :as routing]
            [goog.string :as gstr]))

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

(def line-chart-v2 {:title "Student Map Scores"
                    :width 600
                    :height 400
                    :id "line-chart-v2"})
(def line-chart-v2-dataset
  ;; TODO render this as different vectors
  ;; or multiple datapoints on one vector
  {:reference [{:label "Spring 18" :value 200}
               {:label "Fall 19" :value 210}
               {:label "Winter 19" :value 220}
               {:label "Spring 19" :value 230}
               {:label "Fall 20" :value 240}
               {:label "Winter 20" :value 250}]
   :student [{:label "Spring 18" :value 220}
             {:label "Fall 19" :value 204}
             {:label "Winter 19" :value 225}
             {:label "Spring 19" :value 244}
             {:label "Fall 20" :value 236}
             {:label "Winter 20" :value 228}]})

(def styles {})
;; (def styles {:student-line {:stroke "red"
;;                             :stroke-width 4}
;;              :reference-line {:stroke "green"
;;                               :stroke-dasharray "24 12"}
;;              :legend {:font-size 24}
;;              :axes {:line {:stroke "purple"}
;;                     :text {:fill "orange"}}})

(defn line-chart-v2-page []
  (let [ratom (r/atom {:dataset line-chart-v2-dataset
                       :chart line-chart-v2})]
    [:div
     [:h3 "Line Chart v2 Demo"]
     [line-v2/line-chart {:ratom ratom :styles styles}]]))

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
                                 :value 230
                                 :target 230})
(def tetherball-chart-dataset-4 {:min 220
                                 :max 270
                                 :value 234
                                 :target 230})
(def tetherball-chart-1 {:title "My Tetherball Chart"
                         :width 500
                         :height 260
                         :id "tetherball-graph-1"})
(def tetherball-chart-2 {:title "My Tetherball Chart"
                         :width 500
                         :height 260
                         :id "tetherball-graph-2"})
(def tetherball-chart-3 {:title "My Tetherball Chart"
                         :width 500
                         :height 260
                         :id "tetherball-graph-3"})
(def tetherball-chart-4 {:title "My Tetherball Chart"
                         :width 500
                         :height 260
                         :id "tetherball-graph-4"})

(defn- display-chart-data [{:keys [min target value max]}]
  (gstr/format "Min %d | Target %d | Value %d | Max %d"
               min target value max))

(defn tetherball-chart-page []
  [:div
   [:h3 "Tetherball Demo"]
   (let [ratom (r/atom {:dataset tetherball-chart-dataset-1
                        :chart tetherball-chart-1})]
     [:div
      [:span (display-chart-data tetherball-chart-dataset-1)]
      [tetherball/tetherball-chart {:ratom ratom}]
      [:hr]])
   (let [ratom (r/atom {:dataset tetherball-chart-dataset-2
                        :chart tetherball-chart-2})]
     [:div
      [:span (display-chart-data tetherball-chart-dataset-2)]
      [tetherball/tetherball-chart {:ratom ratom}]
      [:hr]])
   (let [ratom (r/atom {:dataset tetherball-chart-dataset-3
                        :chart tetherball-chart-3})]
     [:div
      [:span (display-chart-data tetherball-chart-dataset-3)]
      [tetherball/tetherball-chart {:ratom ratom}]])
   (let [ratom (r/atom {:dataset tetherball-chart-dataset-4
                        :chart tetherball-chart-4})]
     [:div
      [:span (display-chart-data tetherball-chart-dataset-4)]
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
     [:a {:href (routing/path-for :d3-line-chart-v2)} "Line Chart v2"]]
    [:li
     [:a {:href (routing/path-for :d3-tetherball-chart)} "Tetherball Chart"]]]])
