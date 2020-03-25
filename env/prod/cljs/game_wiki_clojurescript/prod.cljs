(ns game-wiki-clojurescript.prod
  (:require [game-wiki-clojurescript.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
