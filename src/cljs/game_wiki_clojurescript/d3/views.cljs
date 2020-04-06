(ns game-wiki-clojurescript.d3.views
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [goog.object :as gobj]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart Params
(def margin {:top 30 ;; title
             :bottom 20 ;; y axis
             :left 24 ;; x-axis
             :right 0})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
(defn translate [x y]
  (let [x1 (if (nil? x) 0 x)
        y1 (if (nil? y) 0 y)]
    (str "translate(" x "," y ")")))

(defn prepare-dataset [ratom]
  (-> @ratom
      :dataset
      clj->js))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Build chart components
;; All of these take ratom,
;; 1) this is simple
;; 2) does this cause unecessary redrawing?
;; 3) should they only take the direct thing they need?
(defn ->chart-area [ratom]
  (let [{canvas :canvas} @ratom]
    {:x (:left margin)
     :y (:top margin)
     :width (- (:width canvas) (:left margin) (:right margin))
     :height (- (:height canvas) (:top margin) (:bottom margin))}))

; need default bar-colors
(defn ->bar-color [ratom]
  (let [bar-colors (get-in @ratom [:canvas :bar-colors])]
    (fn [i]
      (->> (rem i (count bar-colors))
           (nth bar-colors)))))

(defn ->x-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (->chart-area ratom)
        labels (mapv :label dataset)]
    (-> js/d3
        .scaleBand
        (.rangeRound #js [0 (:width chart-area)])
        (.padding 0.1)
        (.domain (clj->js labels)))))

(defn ->y-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (->chart-area ratom)
        values (mapv :value dataset)
        max-value (apply max values)]
    (-> js/d3
        .scaleLinear
        (.rangeRound #js [(:height chart-area) 0])
        (.domain #js [0 max-value]))))

(defn viz [{:keys [ratom]}]
  [rid3/viz
   {:id "my-graph"
    :ratom ratom
    :svg {:did-mount
          (fn [node ratom]
            (let [{canvas :canvas} @ratom]
              (rid3-> node
                      {:width (:width canvas)
                       :height (:height canvas)})))}
    :main-container {:did-mount
                     (fn [node ratom]
                       (let [chart-area (->chart-area ratom)]
                         (rid3-> node
                                 {:transform (translate (:x chart-area) (:y chart-area))})))}
    :pieces [{:kind :elem-with-data
              :class "bars"
              :tag "rect"
              :prepare-dataset prepare-dataset
              :did-mount
              (fn [node ratom]
                (let [y-scale (->y-scale ratom)
                      x-scale (->x-scale ratom)
                      chart-area (->chart-area ratom)
                      bar-color-fn (->bar-color ratom)]
                  (rid3-> node
                          {:x (fn [d]
                                (x-scale (gobj/get d "label")))
                           :y (fn [d]
                                (y-scale (gobj/get d "value")))
                           :width (.bandwidth x-scale)
                           :fill (fn [d i] (bar-color-fn i))
                           :height (fn [d]
                                     (- (:height chart-area) (y-scale (gobj/get d "value"))))})))}
             {:kind :container
              :class "x-axis"
              :did-mount
              (fn [node ratom]
                (let [x-scale (->x-scale ratom)
                      chart-area (->chart-area ratom)]
                  (rid3-> node
                          {:transform (translate 0 (:height chart-area))}
                          (.call (.axisBottom js/d3 x-scale)))))}
             {:kind :container
              :class "y-axis"
              :did-mount
              (fn [node ratom]
                (let [y-scale (->y-scale ratom)]
                  (rid3-> node
                          (.call (-> (.axisLeft js/d3 y-scale)
                                     (.ticks 3))))))}
             {:kind :elem
              :class "title"
              :tag "text"
              :did-mount
              (fn [node ratom]
                (rid3-> node
                        {:x 0
                         :y -10}
                        (.text (get-in @ratom [:canvas :title]))))}]}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Usage
(def dataset
  [{:label "A" :value 10}
   {:label "B" :value 20}
   {:label "C" :value 35}
   {:label "D" :value 50}])

(def canvas {:title "My Chart"
             :width 200
             :height 200
             :bar-colors ["red" "green" "blue"]})

(defn d3-overview-page []
  (let [ratom (r/atom {:dataset dataset
                       :canvas canvas})]
    [:div
     [:h3 "d3 Demo"]
     [viz {:ratom ratom}]]))
