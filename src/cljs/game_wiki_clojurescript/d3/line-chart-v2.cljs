(ns game-wiki-clojurescript.d3.line-chart-v2
  (:require [reagent.core :as r]
            [d3 :as d3]
            [rid3.core :as rid3 :refer [rid3->]]
            [rid3.attrs :as rid3a]))

;; courtesy of @andrewboltachev
;; https://gist.github.com/danielpcox/c70a8aa2c36766200a95
(defn deep-merge [& maps]
  (apply merge-with (fn [& args]
                      (if (every? #(or (map? %) (nil? %)) args)
                        (apply deep-merge args)
                        (last args)))
         maps))

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
                     :legend {:font-size 18}
                     :axes {:line {:stroke "#aaa"}
                            :text {:fill "#666"}}
                     :floating-axes {:fill "blue"
                                     :font-size 13}})

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
           (.bandwidth x-scale))}))

;; nth but returns nil if i is out of bounds for vec
(defn- safe-nth [vec i]
  (if (and (>= i 0) (< i (count vec)))
    (nth vec i)
    nil))

;;TODO argument ordering
(defn- label-y [i ratom y-scale style]
  (let [line (-> @ratom :dataset :student)
        font-size (:font-size style)
        get-y #(let [val (safe-nth %1 %2)]
                 (if (nil? val)
                   nil
                   (-> val :value y-scale)))
        prior-y (get-y line (dec i))
        point-y (get-y line i)
        next-y (get-y line (inc i))
        above? (cond (nil? prior-y) (<= point-y next-y)
                     (nil? next-y) (>= prior-y point-y)
                     :else (<= point-y (/ (+ prior-y next-y) 2)))]
    (if above?
      (- point-y font-size)
      (+ point-y (* font-size 2)))))

;;TODO argument ordering
(defn- label-x [i ratom y-scale style starting-x]
  (let [line (-> @ratom :dataset :student)
        font-size (:font-size style)
        get-y #(let [val (safe-nth %1 %2)]
                 (if (nil? val)
                   nil
                   (-> val :value y-scale)))
        prior-y (get-y line (dec i))
        point-y (get-y line i)
        next-y (get-y line (inc i))
        ;; TODO calc on the fly (you can base off of label text I think)
        adjustment 25]
    (cond
      ; don't move if 1) on either end or 2)/3) if the point is
      ; larger or smaller than both of its neighbors
      (or (nil? prior-y) (nil? next-y)) starting-x
      (and (>= point-y prior-y) (>= point-y next-y)) starting-x
      (and (<= point-y prior-y) (<= point-y next-y)) starting-x
      ; if sloping up, then move left if point is above the midline, right otherwise
      (>= prior-y point-y next-y) (if (<= point-y (/ (+ prior-y next-y) 2))
                                    (- starting-x adjustment)
                                    (+ starting-x adjustment))
      ; if sloping down, then move right is point is above the midline, left otherwise
      (<= prior-y point-y next-y) (if (<= point-y (/ (+ prior-y next-y) 2))
                                    (+ starting-x adjustment)
                                    (- starting-x adjustment))
      :else starting-x)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart
(defn line-chart [{:keys [ratom styles]}]
  (let [x-scale (->x-scale ratom)
        y-scale (->y-scale ratom)
        chart-area (->chart-area ratom)
        final-styles (deep-merge default-styles styles)
        student-line-style (:student-line final-styles)
        reference-line-style (:reference-line final-styles)
        legend-style (:legend final-styles)
        axes-style (:axes final-styles)
        floating-axes-style (:floating-axes final-styles)
        legend-position (->legend-positions ratom legend-style)]
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
                            (rid3a/attrs student-line-style))))}
               {:kind :elem-with-data
                :class "student-labels"
                :tag "text"
                :prepare-dataset (fn [r] (prepare-dataset r :student))
                :did-mount
                (fn [node ratom]
                  (let [offset-to-center-x (/ (.bandwidth x-scale) 2)]
                    (rid3-> node
                            {:x #(label-x %2
                                          ratom
                                          y-scale
                                          floating-axes-style
                                          (+ (x-scale (.-label %1))
                                             offset-to-center-x))
                             :y #(label-y %2 ratom y-scale floating-axes-style)
                             :text-anchor "middle"}
                            (rid3a/attrs floating-axes-style)
                            (.text #(.-label %)))))}
               {:kind :elem-with-data
                :class "student-points"
                :tag "circle"
                :prepare-dataset (fn [r] (prepare-dataset r :student))
                :did-mount
                (fn [node ratom]
                  (let [offset-to-center-x (/ (.bandwidth x-scale) 2)]
                    (rid3-> node
                            {:cx #(+ (x-scale (.-label %))
                                     offset-to-center-x)
                             :cy #(y-scale (.-value %1))
                             ;;TODO pull from styles
                             :r 4
                             :fill (:stroke student-line-style)})))}
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
                            (rid3a/attrs reference-line-style))))}
              ;; TODO do we want this? it kinda sucks :/
              ;;  {:kind :elem-with-data
              ;;   :class "reference-points"
              ;;   :tag "circle"
              ;;   :prepare-dataset (fn [r] (prepare-dataset r :reference))
              ;;   :did-mount
              ;;   (fn [node ratom]
              ;;     (let [offset-to-center-x (/ (.bandwidth x-scale) 2)]
              ;;       (rid3-> node
              ;;               {:cx #(+ (x-scale (.-label %))
              ;;                        offset-to-center-x)
              ;;                :cy #(y-scale (.-value %1))
              ;;                ;;TODO pull from styles
              ;;                :r 3
              ;;                :fill (:stroke reference-line-style)})))}
               {:kind :container
                :class "x-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          {:transform (translate 0 (:height chart-area))}
                          (.call (.axisBottom js/d3 x-scale))
                          (style-axis axes-style)))}
               {:kind :container
                :class "y-axis"
                :did-mount
                (fn [node ratom]
                  (rid3-> node
                          (.call (-> (.axisLeft js/d3 y-scale)
                                     (.ticks 3)))
                          (style-axis axes-style)))}
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
                              (let [{:keys [stroke]} student-line-style]
                                (rid3-> node
                                        {:y (:y-student legend-position)
                                         :fill stroke}
                                        (rid3a/attrs legend-style)
                                      ;;TODO pull this from dataset
                                        (.text "Student"))))}
                           {:kind :elem
                            :class "reference-legend"
                            :tag "text"
                            :did-mount
                            (fn [node ratom]
                              (let [{:keys [stroke]} reference-line-style]
                                (rid3-> node
                                        {:y (:y-reference legend-position)
                                         :fill stroke}
                                        (rid3a/attrs legend-style)
                                        (.text "Reference"))))}]}]}]))
