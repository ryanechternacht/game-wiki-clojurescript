(ns game-wiki-clojurescript.d3.line-chart-v2
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [rid3.attrs :as rid3a]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart Params
;; TODO move these into the component itself so we can merge
;; user supplied settings into these
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
                     :legend {:font-size 18}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
(defn- translate [x y]
  (let [x (if (nil? x) 0 x)
        y (if (nil? y) 0 y)]
    (str "translate(" x "," y ")")))

(defn- prepare-dataset [ratom line]
  (-> @ratom
      :dataset
      line
      clj->js))

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
        labels (mapv :label (:reference dataset))]
    (-> js/d3
        .scaleBand
        (.rangeRound #js [0 (:width chart-area)])
        (.padding 0.1)
        (.domain (clj->js labels)))))

(defn- ->y-scale [ratom]
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

;; returns the new position of y1 based on y2's position
;; and how much buffer should be between them. on-tie should
;; be + or - based on which way to move y1 if it has the same
;; position as y2
(defn- avoid-y-overlap [y1 y2 buffer on-tie]
  (let [pos-diff (Math/abs (- y1 y2))
        split (/ (- buffer pos-diff) 2)]
    (if (< pos-diff split)
      (if (= y1 y2)
        (on-tie y1 split)
        (if (> y1 y2)
          (+ y1 split)
          (- y1 split)))
      y1)))

; TODO this seems like a clusterfuck
(defn- ->legend-positions [ratom legend]
  (let [y-scale (->y-scale ratom)
        x-scale (->x-scale ratom)
        student-final (-> @ratom :dataset :student last)
        reference-final (-> @ratom :dataset :reference last)
        font-size (:font-size legend)
        center-y #(+ %1 (/ font-size 2) -2)
        y-student (-> (:value student-final) y-scale center-y)
        y-reference (-> (:value reference-final) y-scale center-y)
        overlap-zone (+ font-size 2)]
    {:y-student (avoid-y-overlap y-student y-reference overlap-zone -)
     :y-reference (avoid-y-overlap y-reference y-student overlap-zone +)
     :x (+ (x-scale (:label student-final))
           (* (/ (.bandwidth x-scale) 4) 3))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart
(defn line-chart [{:keys [ratom styles]}]
  (let [x-scale (->x-scale ratom)
        y-scale (->y-scale ratom)
        chart-area (->chart-area ratom)
        student-line (merge (:student-line default-styles)
                            (:student-line styles))
        reference-line (merge (:reference-line default-styles)
                              (:reference-line styles))
        legend (merge (:legend default-styles)
                      (:legend styles))
        legend-position (->legend-positions ratom legend)]
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
                         (rid3-> node
                                 {:transform (translate (:x chart-area)
                                                        (:y chart-area))}))}
      :pieces [{:kind :elem
                :class "student-line"
                :tag "path"
                :did-mount
                (fn [node ratom]
                  (let [offset-to-center-x (/ (.bandwidth x-scale) 2)]
                    (rid3-> node
                            (.datum (prepare-dataset ratom :student))
                            {:d (-> (.line js/d3)
                                    (.x #(+ (x-scale (.-label %))
                                            offset-to-center-x))
                                    (.y #(y-scale (.-value %))))}
                            (rid3a/attrs student-line))))}
               {:kind :elem
                :class "reference-line"
                :tag "path"
                :did-mount
                (fn [node ratom]
                  (let [offset-to-center-x (/ (.bandwidth x-scale) 2)]
                    (rid3-> node
                            (.datum (prepare-dataset ratom :reference))
                            {:d (-> (.line js/d3)
                                    (.x #(+ (x-scale (.-label %))
                                            offset-to-center-x))
                                    (.y #(y-scale (.-value %))))}
                            (rid3a/attrs reference-line))))}
               {:kind :container
                :class "x-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          {:transform (translate 0 (:height chart-area))}
                          (.call (.axisBottom js/d3 x-scale))))}
               {:kind :container
                :class "y-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          (.call (-> (.axisLeft js/d3 y-scale)
                                     (.ticks 3)))))}
               {:kind :container
                :class "legend"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          {:transform (translate (:x legend-position) 0)}))
                :children [{:kind :elem
                            :class "student-legend"
                            :tag "text"
                            :did-mount
                            (fn [node ratom]
                              (let [{:keys [stroke]} student-line
                                    {:keys [font-size]} legend]
                                (rid3-> node
                                        {:y (:y-student legend-position)
                                         :fill stroke
                                         :font-size font-size}
                                      ;;TODO pull this
                                        (.text "Student"))))}
                           {:kind :elem
                            :class "reference-legend"
                            :tag "text"
                            :did-mount
                            (fn [node ratom]
                              (let [{:keys [stroke]} reference-line]
                                (rid3-> node
                                        {:y (:y-reference legend-position)
                                         :fill stroke}
                                        (rid3a/attrs legend)
                                        (.text "Reference"))))}]}]}]))
