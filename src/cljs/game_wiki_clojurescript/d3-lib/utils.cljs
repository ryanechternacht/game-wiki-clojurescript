(ns game-wiki-clojurescript.d3-lib.utils)

;; courtesy of @andrewboltachev
;; https://gist.github.com/danielpcox/c70a8aa2c36766200a95
(defn deep-merge [& maps]
  (apply merge-with (fn [& args]
                      (if (every? #(or (map? %) (nil? %)) args)
                        (apply deep-merge args)
                        (last args)))
         maps))