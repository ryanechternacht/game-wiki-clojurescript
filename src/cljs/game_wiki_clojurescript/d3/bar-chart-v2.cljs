(ns game-wiki-clojurescript.d3.bar-chart-v2
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [rid3.attrs :as rid3a]
            [goog.object :as g]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
(defn- translate [x y]
  (let [x (if (nil? x) 0 x)
        y (if (nil? y) 0 y)]
    (str "translate(" x "," y ")")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart Params
(def margin {:top 40
             :bottom 40
             :left 40
             :right 80})

(def default-styles {:student-line {:stroke "blue"
                                    :stroke-width 2
                                    :fill "none"}
                     :reference-line {:stroke "#aaa"
                                      :stroke-dasharray "8 4"
                                      :stroke-width 1
                                      :fill "none"}
                     :legend {:font-size 18}
                     :axes {:line {:stroke "#aaa"}
                            :text {:fill "#666"}}})



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Build chart components
(defn- ->chart-area [ratom]
  (let [{chart :chart} @ratom]
    {:x (:left margin)
     :y (:top margin)
     :width (- (:width chart) (:left margin) (:right margin))
     :height (- (:height chart) (:top margin) (:bottom margin))}))

(defn- ->x-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (->chart-area ratom)
        labels ["data"]]
    (-> js/d3
        .scaleBand
        (.rangeRound #js [0 (:width chart-area)])
        (.padding 0.1)
        (.domain (clj->js labels)))))

(defn- ->y-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (->chart-area ratom)]
    (-> js/d3
        .scaleLinear
        (.rangeRound #js [(:height chart-area) 0])
        (.domain #js [(:min dataset) (:max dataset)]))))

(defn prepare-dataset [ratom]
  (clj->js [(:dataset @ratom)]))

(defn bar-chart [{:keys [ratom styles]}]
  (let [{:keys [chart dataset]} @ratom
        chart-area (->chart-area ratom)
        x-scale (->x-scale ratom)
        y-scale (->y-scale ratom)]
    [rid3/viz
     {:id "my-graph" ;; TODO
      :ratom ratom
      :svg {:did-mount
            (fn [node ratom]
              (rid3-> node
                      {:width (:width chart)
                       :height (:height chart)}))}
      :main-container {:did-mount
                       (fn [node ratom]
                         (rid3-> node
                                 {:transform (translate (:x chart-area)
                                                        (:y chart-area))}))}
      :pieces [{:kind :elem-with-data
                :class "bar"
                :tag "rect"
                :prepare-dataset prepare-dataset
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          {:x (x-scale "data")
                           :y #(y-scale (.-value %))
                           :width (.bandwidth x-scale)
                           :height #(- (:height chart-area) (y-scale (.-value %)))}
                          (.on "mouseover"
                               (fn [d i]
                                 (this-as this
                                          (let [mouse-value (g/get d "mouse-value")]
                                            (rid3-> (.select d3 this)
                                                    .transition
                                                    (.duration 500)
                                                    (.ease js/d3.easeCubic)
                                                    {:y (y-scale mouse-value)
                                                     :height (- (:height chart-area)
                                                                (y-scale mouse-value))})))))
                          (.on "mouseout"
                               (fn [d i]
                                 (this-as this
                                          (let [value (.-value d)]
                                            (rid3-> (.select d3 this)
                                                    .transition
                                                    (.duration 500)
                                                    (.ease js/d3.easeCubic)
                                                    {:y (y-scale value)
                                                     :height (- (:height chart-area)
                                                                (y-scale value))})))))))}
               {:kind :container
                :class "x-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          {:transform (translate 0 (:height chart-area))}
                          (.call (.axisBottom js/d3 x-scale))
                          ;; (style-axis axes-style)
                          ))}
               {:kind :container
                :class "y-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          (.call (-> (.axisLeft js/d3 y-scale)
                                     (.ticks 3)))
                          ;; (style-axis axes-style)
                          ))}]}]))