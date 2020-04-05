(ns game-wiki-clojurescript.d3.views
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [goog.object :as gobj]))

(def dimensions {:width 200
                 :height 200})


(def data
  {:dataset [{:label "A" :value 10}
             {:label "B" :value 20}
             {:label "C" :value 30}]})

(defn prepare-dataset [ratom]
  (-> @ratom
      :dataset
      clj->js))

(def color
  (-> js/d3
      (.scaleOrdinal #js ["red" "green" "blue"])))

(defn ->x-scale [ratom]
  (let [{:keys [dataset]} @ratom
        labels (mapv :label dataset)]
    (-> js/d3
        .scaleBand
        (.rangeRound #js [0 (:width dimensions)])
        (.padding 0.1)
        (.domain (clj->js labels)))))

(defn ->y-scale [ratom]
  (let [{:keys [dataset]} @ratom
        values (mapv :value dataset)
        max-value (apply max values)]
    (-> js/d3
        .scaleLinear
        (.rangeRound #js [(:height dimensions) 0])
        (.domain #js [0 max-value]))))

(defn viz [{:keys [ratom]}]
  [rid3/viz
   {:id "my-graph"
    :ratom ratom
    :svg {:did-mount
          (fn [node ratom]
            (rid3-> node
                    {:width (:width dimensions)
                     :height (:height dimensions)}))}
    :pieces [{:kind :elem-with-data
              :class "bars"
              :tag "rect"
              :prepare-dataset prepare-dataset
              :did-mount
              (fn [node ratom]
                (let [y-scale (->y-scale ratom)
                      x-scale (->x-scale ratom)]
                  (rid3-> node
                          {:x (fn [d]
                                (x-scale (gobj/get d "label")))
                           :y (fn [d]
                                (y-scale (gobj/get d "value")))
                           :width (.bandwidth x-scale)
                           :fill (fn [d i] (color i))
                           :height (fn [d]
                                     (- (:height dimensions) (y-scale (gobj/get d "value"))))})))}]}])

(defn d3-overview-page []
  (let [ratom (r/atom data)]
    [:div
     [:h3 "d3 Demo"]
     (let [data (r/atom data)]
       [viz {:ratom data}])]))

