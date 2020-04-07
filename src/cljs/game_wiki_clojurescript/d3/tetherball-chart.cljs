(ns game-wiki-clojurescript.d3.tetherball-chart
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [goog.string :as gstr]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart Params
(def margin {:top 10
             :bottom 10
             :left 36
             :right 36})

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

;;TODO get back to this instead of css?
;; (def grey-line "#aaa")
;; (def grey-text "#666")
;; (defn lighten-axis [node]
;;   (rid3-> node
;;           (.select "path")
;;           {:style {:stroke grey-line}})
;;   (rid3-> node
;;           (.selectAll ".tick text")
;;           {:style {:fill grey-text}})
;;   (rid3-> node
;;           (.selectAll ".tick line")
;;           {:style {:stroke grey-line}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Build chart components
;; All of these take ratom,
;; 1) this is easy
;; 2) does this cause unecessary redrawing?
;; 3) should they only take the direct thing they need?
(defn ->chart-area [ratom]
  (let [{chart :chart} @ratom]
    {:x (:left margin)
     :y (:top margin)
     :width (- (:width chart) (:left margin) (:right margin))
     :height (- (:height chart) (:top margin) (:bottom margin))}))

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
              :class "tetherball-line"
              :tag "line"
              :did-mount
              (fn [node ratom]
                (let [chart-area (->chart-area ratom)
                      y (/ (:height chart-area) 2)
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
                      y (/ (:height chart-area) 2)]
                  (rid3-> node
                          {:transform (translate 0 y)})))
              :children [{:kind :elem
                          :class "tetherball-line"
                          :tag "line"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:x1 1 :x2 1 :y1 0 :y2 5
                                     :stroke-width 2
                                     :fill "none"}))}
                         ;;TODO get these centered?
                         {:kind :elem
                          :class "tetherball-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 16 :x -24}
                                    (.text "Lowest Grade")))}
                         {:kind :elem
                          :class "tetherball-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 28 :x -24}
                                    (.text "Level Score")))}]}
             {:kind :container
              :class "right-axis"
              :did-mount
              (fn [node ratom]
                (let [chart-area (->chart-area ratom)
                      y (/ (:height chart-area) 2)
                      right (:width chart-area)]
                  (rid3-> node
                          {:transform (translate right y)})))
              ;;TODO center or right align these?
              :children [{:kind :elem
                          :class "tetherball-line"
                          :tag "line"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:x1 -1 :x2 -1 :y1 0 :y2 5
                                     :stroke-width 2
                                     :fill "none"}))}
                         ;;TODO get these centered?
                         {:kind :elem
                          :class "tetherball-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 16 :x -30}
                                    (.text "Highest Grade")))}
                         {:kind :elem
                          :class "tetherball-label"
                          :tag "text"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:y 28 :x -30}
                                    (.text "Level Score")))}]}
             {:kind :container
              :class "target"
              :did-mount
              (fn [node ratom]
                (let [x ((->x-scale ratom) (get-in @ratom [:dataset :target]))]
                  (rid3-> node
                          {:transform (translate x 0)})))
              :children [{:kind :elem
                          :class "target-line"
                          :tag "line"
                          :did-mount
                          (fn [node ratom]
                            (let [height (:height (->chart-area ratom))]
                              (rid3-> node
                                      {:x1 0 :y1 0 :x2 0 :y2 height
                                       :fill "none"
                                       :stroke-dasharray "8 4"})))}
                         {:kind :container
                          :class "target-box"
                          :did-mount
                          (fn [node ratom]
                            (let [middle-y (/ (:height (->chart-area ratom)) 2)
                                  height 40
                                  width 40]
                              (rid3-> node
                                      {:transform (translate (/ (- width) 2) (- middle-y (/ height 2)))})))
                          :children [{:kind :elem
                                      :class "target-background"
                                      :tag "rect"
                                      :did-mount
                                      (fn [node ratom]
                                        (let [height 40
                                              width 40]
                                          (rid3-> node
                                                  {:height height
                                                   :width width
                                                   :fill "white"})))}
                                     ;;TODO center text
                                     {:kind :elem
                                      :class "target-label"
                                      :tag "text"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:y 10
                                                 :x 5}
                                                (.text "Grade")))}
                                     {:kind :elem
                                      :class "target-label"
                                      :tag "text"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:y 23
                                                 :x 5}
                                                (.text "Level")))}
                                     {:kind :elem
                                      :class "target-label"
                                      :tag "text"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:y 36
                                                 :x 5}
                                                (.text "Norm")))}]}]}
             {:kind :container
              :class "value"
              :did-mount
              (fn [node ratom]
                (let [x ((->x-scale ratom) (get-in @ratom [:dataset :value]))]
                  (rid3-> node
                          {:transform (translate x 0)})))
              :children [{:kind :container
                          :class "value-circle"
                          :did-mount
                          (fn [node ratom]
                            (let [middle-y (/ (:height (->chart-area ratom)) 2)]
                              (rid3-> node
                                      {:transform (translate 0 middle-y)})))
                          :children [{:kind :elem
                                      :class "value-circle"
                                      :tag "circle"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:r 20
                                                 :fill "white"
                                                 :stroke "black"}))}
                                     {:kind :elem
                                      :class "value-score"
                                      :tag "text"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                ;;TODO lot's of magic numbers here?
                                                {:y 5
                                                 :x -14}
                                                (.text (get-in @ratom [:dataset :value]))))}]}
                         {:kind :container
                          :class "value-header"
                          :did-mount
                          (fn [node ratom]
                            (let [middle-y (/ (:height (->chart-area ratom)) 2)
                                  y (- middle-y 25)
                                  x -20]
                              (rid3-> node
                                      {:transform (translate x y)})))
                          :children [{:kind :elem
                                      :class "value-checkbox"
                                      :tag "rect"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:fill "white"
                                                 :stroke "black"
                                                 :y -8
                                                 :width 8
                                                 :height 8}))}
                                     {:kind :elem
                                      :class "value-check"
                                      :tag "path"
                                      :did-mount
                                      (fn [node ratom]
                                        (let [dataset (:dataset @ratom)
                                              render? (> (:value dataset) (:target dataset))]
                                          (prn dataset)
                                          (prn render?)
                                          (when render?
                                            (rid3-> node
                                                    {:d "M 1 -6 l 2 4 l 6 -10"
                                                     :stroke-width 2
                                                     :stroke "green"
                                                     :fill "none"}))))}
                                     {:kind :elem
                                      :class "value-label"
                                      :tag "text"
                                      :did-mount
                                      (fn [node ratom]
                                        (rid3-> node
                                                {:x 12}
                                                (.text "Goal Met")))}]}]}]}])
