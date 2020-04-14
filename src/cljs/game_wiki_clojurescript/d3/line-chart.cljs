(ns game-wiki-clojurescript.d3.line-chart
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
  (let [x (if (nil? x) 0 x)
        y (if (nil? y) 0 y)]
    (str "translate(" x "," y ")")))

(defn prepare-dataset [ratom]
  (-> @ratom
      :dataset
      clj->js))

(def grey-line "#aaa")
(def grey-text "#666")
(defn lighten-axis [node]
  (rid3-> node
          (.select "path")
          {:style {:stroke grey-line}})
  (rid3-> node
          (.selectAll ".tick text")
          {:style {:fill grey-text}})
  (rid3-> node
          (.selectAll ".tick line")
          {:style {:stroke grey-line}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Build chart components
;; All of these take ratom,
;; 1) this is simple
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

(defn line-chart [{:keys [ratom]}]
  [rid3/viz
   {:id "my-graph"
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
              :class "line"
              :tag "path"
              :did-mount
              (fn [node ratom]
                (let [y-scale (->y-scale ratom)
                      x-scale (->x-scale ratom)]
                  (rid3-> node
                          (.datum (prepare-dataset ratom))
                          {:d (-> (.line js/d3)
                                  (.x #(+ (x-scale (.-label %))
                                          (/ (.bandwidth x-scale) 2)))
                                  (.y #(y-scale (.-value %))))
                           :stroke (get-in @ratom [:chart :line-color])
                           :stroke-width 1.5
                           :fill "none"})))}
             {:kind :container
              :class "x-axis"
              :did-mount
              (fn [node ratom]
                (let [x-scale (->x-scale ratom)
                      chart-area (->chart-area ratom)]
                  (rid3-> node
                          {:transform (translate 0 (:height chart-area))}
                          (.call (.axisBottom js/d3 x-scale))
                          lighten-axis)))}
             {:kind :container
              :class "y-axis"
              :did-mount
              (fn [node ratom]
                (let [y-scale (->y-scale ratom)]
                  (rid3-> node
                          (.call (-> (.axisLeft js/d3 y-scale)
                                     (.ticks 3)))
                          lighten-axis)))}
             {:kind :elem
              :class "title"
              :tag "text"
              :did-mount
              (fn [node ratom]
                (rid3-> node
                        {:x 0
                         :y -10}
                        (.text (get-in @ratom [:chart :title]))))}]}])
