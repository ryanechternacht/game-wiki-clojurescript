(ns game-wiki-clojurescript.d3.line-chart-v2
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart Params
(def margin {:top 40
             :bottom 40
             :left 40
             :right 0})

(def student-line {:color "lightblue"
                   :width 2})
(def reference-line {:color "#aaa"
                     :dashes "8 4"
                     :width 1})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
(defn translate [x y]
  (let [x (if (nil? x) 0 x)
        y (if (nil? y) 0 y)]
    (str "translate(" x "," y ")")))

(defn prepare-dataset [ratom line]
  (-> @ratom
      :dataset
      line
      clj->js))

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
        labels (mapv :label (:reference dataset))]
    (-> js/d3
        .scaleBand
        (.rangeRound #js [0 (:width chart-area)])
        (.padding 0.1)
        (.domain (clj->js labels)))))

(defn ->y-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (->chart-area ratom)
        values (mapv :value (:reference dataset))
        max-value (apply max values)
        min-value (apply min values)]
    (-> js/d3
        .scaleLinear
        (.rangeRound #js [(:height chart-area) 0])
        ;;TODO Magic number
        (.domain #js [(- min-value 10) (+ 10 max-value)]))))

(defn line-chart [{:keys [ratom]}]
  [rid3/viz
   {:id (get-in @ratom [:chart :id])
    :ratom ratom
    :svg {:did-mount
          (fn [node ratom]
            (let [{:keys [chart]} @ratom]
              (rid3-> node
                      {:width (:width chart)
                       :height (:height chart)})))}
    :main-container {:did-mount
                     (fn [node ratom]
                       (let [chart-area (->chart-area ratom)]
                         (rid3-> node
                                 {:transform (translate (:x chart-area)
                                                        (:y chart-area))})))}
    :pieces [{:kind :elem
              :class "student-line"
              :tag "path"
              :did-mount
              (fn [node ratom]
                (let [y-scale (->y-scale ratom)
                      x-scale (->x-scale ratom)
                      offset-to-center-x (/ (.bandwidth x-scale) 2)
                      {:keys [color width]} student-line]
                  (rid3-> node
                          (.datum (prepare-dataset ratom :student))
                          {:d (-> (.line js/d3)
                                  (.x #(+ (x-scale (.-label %))
                                          offset-to-center-x))
                                  (.y #(y-scale (.-value %))))
                           :stroke color
                           :stroke-width width
                           :fill "none"})))}

             {:kind :elem
              :class "reference-line"
              :tag "path"
              :did-mount
              (fn [node ratom]
                (let [y-scale (->y-scale ratom)
                      x-scale (->x-scale ratom)
                      offset-to-center-x (/ (.bandwidth x-scale) 2)
                      {:keys [color dashes width]} reference-line]
                  (rid3-> node
                          (.datum (prepare-dataset ratom :reference))
                          {:d (-> (.line js/d3)
                                  (.x #(+ (x-scale (.-label %))
                                          offset-to-center-x))
                                  (.y #(y-scale (.-value %))))
                           :stroke color
                           :stroke-width width
                           :fill "none"
                           :stroke-dasharray dashes})))}
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
                                     (.ticks 3))))))}]}])
