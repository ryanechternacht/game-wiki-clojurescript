(ns game-wiki-clojurescript.d3.line-chart-v3
  (:require [game-wiki-clojurescript.d3-lib.line-chart :refer [render]]
            [reagent.core :as r]))

(def dataset
  {:lines [[{:label "Spring 18" :value 200}
            {:label "Fall 19" :value 210}
            {:label "Winter 19" :value 220}
            {:label "Spring 19" :value 230}
            {:label "Fall 20" :value 240}
            {:label "Winter 20" :value 250}]
           [{:label "Spring 18" :value 220}
            {:label "Fall 19" :value 198}
            {:label "Winter 19" :value 224}
            {:label "Spring 19" :value 244}
            {:label "Fall 20" :value 236}
            {:label "Winter 20" :value 260}]]})
  ;; ;; TODO render this as different vectors
  ;; ;; or multiple datapoints on one vector
  ;; {:reference [{:label "Spring 18" :value 200}
  ;;              {:label "Fall 19" :value 210}
  ;;              {:label "Winter 19" :value 220}
  ;;              {:label "Spring 19" :value 230}
  ;;              {:label "Fall 20" :value 240}
  ;;              {:label "Winter 20" :value 250}]
  ;;  :student [{:label "Spring 18" :value 220}
  ;;            {:label "Fall 19" :value 204}
  ;;            {:label "Winter 19" :value 224}
  ;;            {:label "Spring 19" :value 244}
  ;;            {:label "Fall 20" :value 236}
  ;;            {:label "Winter 20" :value 250}]})

(def chart {:title "Student Map Scores"
            :width 600
            :height 400
            :id "line-chart-v2"})

(def styles {:student-line {:stroke "red"
                            :stroke-width 4}
             :reference-line {:stroke "green"
                              :stroke-dasharray "24 12"}
             :legend {:font-size 24}
             :axes {:line {:stroke "purple"}
                    :text {:fill "orange"}}})

(defn line-chart [{:keys [ratom styles]}]
  (let [ratom (r/atom {:dataset dataset
                       :chart chart
                       :styles styles})]
    [:div "todo"
     [render {:ratom ratom}]]))
