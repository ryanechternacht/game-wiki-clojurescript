(ns game-wiki-clojurescript.d3.tetherball-chart
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [goog.string :as gstr]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart Constants
(def margin {:top 24
             :bottom 24
             :left 36
             :right 36})

(def value-marker {:circle-radius 40
                   :checkbox-size 14})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
(defn translate [x y]
  (let [x1 (if (nil? x) 0 x)
        y1 (if (nil? y) 0 y)]
    (gstr/format "translate(%d,%d)" x y)))

(defn prepare-dataset [ratom]
  (-> @ratom
      :dataset
      clj->js))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Build chart components
;; All of these take ratom,
;; 1) this is easy
;; 2) does this cause unecessary redrawing?
;; 3) should they only take the direct thing they need?
(defn ->chart-area [ratom]
  (let [{chart :chart} @ratom]
    (let [height (- (:height chart) (:top margin) (:bottom margin))]
      {:x (:left margin)
       :y (:top margin)
       :width (- (:width chart) (:left margin) (:right margin))
       :height height
       :middle-line (/ height 2)})))

(defn ->x-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (->chart-area ratom)]
    (-> js/d3
        .scaleLinear
        (.rangeRound #js [0 (:width chart-area)])
        (.domain #js [(:min dataset) (:max dataset)]))))

(defn tetherball-chart [{:keys [ratom]}]
  [rid3/viz
   {:id (get-in @ratom [:chart :id])
    :ratom ratom
    :svg {:did-mount
          (fn [node ratom]
            (let [{chart :chart} @ratom]
              (rid3-> node
                      {:width (:width chart)
                       :height (:height chart)})))}
    :main-container {:did-mount
                     (fn [node ratom]
                       (let [chart-area (->chart-area ratom)]
                         (rid3-> node
                                 {:transform (translate (:x chart-area) (:y chart-area))})))}
    :pieces [{:kind :elem
              :class "tetherball-axis-line"
              :tag "line"
              :did-mount
              (fn [node ratom]
                (let [chart-area (->chart-area ratom)
                      y (:middle-line chart-area)
                      right (:width chart-area)]
                  (rid3-> node
                          {:x1 0 :x2 right :y1 y :y2 y
                           :stroke-width 2
                           :fill "none"})))}
             {:kind :container
              :class "left-axis"
              :did-mount
              (fn [node ratom]
                (let [chart-area (->chart-area ratom)
                      y (:middle-line chart-area)]
                  (rid3-> node
                          {:transform (translate 0 y)})))
              :children [{:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 16}
                                    (.text "Lowest")))}
                         {:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 28}
                                    (.text "Grade")))}
                         {:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 40}
                                    (.text "Level")))}
                         {:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 52}
                                    (.text "Score")))}]}

             {:kind :container
              :class "right-axis"
              :did-mount
              (fn [node ratom]
                (let [chart-area (->chart-area ratom)
                      y (:middle-line chart-area)
                      right (:width chart-area)]
                  (rid3-> node
                          {:transform (translate right y)})))
              :children [{:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 16}
                                    (.text "Highest")))}
                         {:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 28}
                                    (.text "Grade")))}
                         {:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 40}
                                    (.text "Level")))}
                         {:kind :elem
                          :class "tetherball-axis-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 52}
                                    (.text "Score")))}]}
             {:kind :container
              :class "target"
              :did-mount
              (fn [node ratom]
                (let [x ((->x-scale ratom) (get-in @ratom [:dataset :target]))]
                  (rid3-> node
                          {:transform (translate x 0)})))
              :children [{:kind :elem
                          :class "tetherball-target-line"
                          :tag "line"
                          :did-mount
                          (fn [node ratom]
                            (let [height (:height (->chart-area ratom))]
                              (rid3-> node
                                      {:x1 0 :y1 0 :x2 0 :y2 height
                                       :stroke-dasharray "8 4"})))}
                         {:kind :elem
                          :class "tetherball-target-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (let [dataset (:dataset @ratom)
                                  target-met (> (:value dataset) (:target dataset))]
                              (rid3-> node
                                      {:y 12 :x (if target-met -5 5)
                                       :class (when target-met "svg-align-right")}
                                      (.text "Grade Level Norm"))))}]}
             {:kind :container
              :class "value"
              :did-mount
              (fn [node ratom]
                (let [x ((->x-scale ratom) (get-in @ratom [:dataset :value]))
                      y (:middle-line (->chart-area ratom))]
                  (rid3-> node
                          {:transform (translate x y)})))
              :children [{:kind :container
                          :class "tetherball-value-marker"
                          :did-mount (fn [node ratom] node)
                          :children [{:kind :elem
                                      :class "tetherball-value-circle"
                                      :tag "circle"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:r (:circle-radius value-marker)}))}
                                     {:kind :elem
                                      :class "tetherball-value-score"
                                      :tag "text"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                (.text (get-in @ratom [:dataset :value]))))}]}
                         {:kind :container
                          :class "value-header"
                          :did-mount
                          (fn [node ratom]
                            (let [r (:circle-radius value-marker)
                                  y (- 0 r 10)
                                  x (- r)]
                              (rid3-> node
                                      {:transform (translate x y)})))
                          :children [{:kind :elem
                                      :class "tetherball-value-checkbox"
                                      :tag "rect"
                                      :did-mount
                                      (fn [node ratom]
                                        (let [l (:checkbox-size value-marker)]
                                          (rid3-> node
                                                  {:y (- l)
                                                   :width l
                                                   :height l})))}
                                     {:kind :elem
                                      :class "tetherball-value-check"
                                      :tag "path"
                                      :did-mount
                                      (fn [node ratom]
                                        (let [dataset (:dataset @ratom)
                                              render? (> (:value dataset) (:target dataset))]
                                          (prn dataset)
                                          (prn render?)
                                          (when render?
                                            ;;TODO this is really hardcoded
                                            (rid3-> node
                                                    {:d "M 2 -10 l 5 8 l 7 -20"}))))}
                                     {:kind :elem
                                      :class "tetherball-value-label"
                                      :tag "text"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:x (+ (:checkbox-size value-marker) 4)}
                                                (.text "Goal Met")))}]}]}]}])
