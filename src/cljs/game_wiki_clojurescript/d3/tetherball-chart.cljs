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

;; (defn ->x-scale [ratom]
;;   (let [{:keys [dataset]} @ratom
;;         chart-area (->chart-area ratom)
;;         labels (mapv :label dataset)]
;;     (-> js/d3
;;         .scaleBand
;;         (.rangeRound #js [0 (:width chart-area)])
;;         (.padding 0.1)
;;         (.domain (clj->js labels)))))
(defn ->x-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (->chart-area ratom)]
    (-> js/d3
        .scaleLinear
        (.rangeRound #js [0 (:width chart-area)])
        (.domain #js [(:min dataset) (:max dataset)]))))

;; (defn ->y-scale [ratom]
;;   (let [{:keys [dataset]} @ratom
;;         chart-area (->chart-area ratom)
;;         values (mapv :value dataset)
;;         max-value (apply max values)]
;;     (-> js/d3
;;         .scaleLinear
;;         (.rangeRound #js [(:height chart-area) 0])
;;         (.domain #js [0 max-value]))))

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
              :tag "path"
              :did-mount
              (fn [node ratom]
                (let [chart-area (->chart-area ratom)
                      y (/ (:height chart-area) 2)
                      right (:width chart-area)]
                  (rid3-> node
                          {:d (gstr/format "M 0 %d H %d" y right)
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
                          :tag "path"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:d "M 1 0 V 5"
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
                          :tag "path"
                          :did-mount
                          (fn [node ratom]
                            (rid3-> node
                                    {:d "M -1 0 V 5"
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
                                       :stroke "black"
                                       :stroke-dasharray "8 4"})))}]}]}])

;;     :pieces [{:kind :elem-with-data
;;               :class "line"xw
;;               :tag "path"
;;               :did-mount
;;               (fn [node ratom]
;;                 (let [y-scale (->y-scale ratom)
;;                       x-scale (->x-scale ratom)
;;                       chart-area (->chart-area ratom)]
;;                   (rid3-> node
;;                           (.datum (prepare-dataset ratom))
;;                           {:d (-> (.line js/d3)
;;                                   (.x #(+ (x-scale (. % -label))
;;                                           (/ (.bandwidth x-scale) 2)))
;;                                   (.y #(y-scale (. % -value))))
;;                            :stroke (get-in @ratom [:chart :line-color])
;;                            :stroke-width 1.5
;;                            :fill "none"})))}
;;              {:kind :container
;;               :class "x-axis"
;;               :did-mount
;;               (fn [node ratom]
;;                 (let [x-scale (->x-scale ratom)
;;                       chart-area (->chart-area ratom)]
;;                   (rid3-> node
;;                           {:transform (translate 0 (:height chart-area))}
;;                           (.call (.axisBottom js/d3 x-scale))
;;                           lighten-axis)))}
;;              {:kind :container
;;               :class "y-axis"
;;               :did-mount
;;               (fn [node ratom]
;;                 (let [y-scale (->y-scale ratom)]
;;                   (rid3-> node
;;                           (.call (-> (.axisLeft js/d3 y-scale)
;;                                      (.ticks 3)))
;;                           lighten-axis)))}
;;              {:kind :elem
;;               :class "title"
;;               :tag "text"
;;               :did-mount
;;               (fn [node ratom]
;;                 (rid3-> node
;;                         {:x 0
;;                          :y -10}
;;                         (.text (get-in @ratom [:chart :title]))))}]}])
