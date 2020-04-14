(ns game-wiki-clojurescript.d3-lib.utils)

;; courtesy of @andrewboltachev
;; https://gist.github.com/danielpcox/c70a8aa2c36766200a95
(defn deep-merge [& maps]
  (apply merge-with (fn [& args]
                      (if (every? #(or (map? %) (nil? %)) args)
                        (apply deep-merge args)
                        (last args)))
         maps))

(defn translate [x y]
  (let [x (if (nil? x) 0 x)
        y (if (nil? y) 0 y)]
    (str "translate(" x "," y ")")))

;; TODO take :chart piece instead of ratom?
(defn ->chart-area [ratom margin]
  (let [{chart :chart} @ratom]
    {:x (:left margin)
     :y (:top margin)
     :width (- (:width chart) (:left margin) (:right margin))
     :height (- (:height chart) (:top margin) (:bottom margin))}))

(defn prepare-line-dataset [ratom line]
  (-> @ratom
      :dataset
      line
      clj->js))