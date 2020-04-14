(ns game-wiki-clojurescript.d3-lib.line-chart
  (:require [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [rid3.attrs :as rid3a]
            [game-wiki-clojurescript.d3-lib.utils :as utils]))

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

;; TODO Can be utils?
(defn style-axis [node axis-style]
  (let [line-style (:line axis-style)
        text-style (:text axis-style)]
    (do
      (rid3-> node
              (.select "path")
              (rid3a/attrs line-style))
      (rid3-> node
              (.selectAll ".tick line")
              (rid3a/attrs line-style))
      (rid3-> node
              (.selectAll ".tick text")
              (rid3a/attrs text-style)))))

;; TODO set axis explicitly vs. using one of the lines
(defn- ->x-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (utils/->chart-area ratom margin)
        labels (mapv :label (first (:lines dataset)))]
    (-> js/d3
        .scaleBand
        (.rangeRound #js [0 (:width chart-area)])
        (.padding 0.1)
        (.domain (clj->js labels)))))

(defn- ->y-scale [ratom]
  (let [{:keys [dataset]} @ratom
        chart-area (utils/->chart-area ratom margin)
        ;;TODO do these with transducers?
        data-points (reduce into (:lines dataset))
        values (mapv :value data-points)
        max-value (apply max values)
        min-value (apply min values)]
    (-> js/d3
        .scaleLinear
        (.rangeRound #js [(:height chart-area) 0])
        ;;TODO Magic number
        (.domain #js [(- min-value 10) (+ 10 max-value)]))))

(defn render [{:keys [ratom]}]
  (prn @ratom)
  (let [x-scale (->x-scale ratom)
        y-scale (->y-scale ratom)
        chart-area (utils/->chart-area ratom margin)
        axes-style (:axes default-styles)]
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
                                 {:transform (utils/translate (:x chart-area)
                                                              (:y chart-area))}))}
      :pieces [{:kind :container
                :class "x-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          {:transform (utils/translate 0 (:height chart-area))}
                          (.call (.axisBottom js/d3 x-scale))
                          (style-axis axes-style)))}
               {:kind :container
                :class "y-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          (.call (-> (.axisLeft js/d3 y-scale)
                                     (.ticks 3)))
                          (style-axis axes-style)))}]}]))