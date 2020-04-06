(ns game-wiki-clojurescript.routing
  (:require [reitit.frontend :as reitit]
            [accountant.core :as accountant]))

;; -------------------------
;; Routes
;; TODO Can we push this out to the modules?
(def router
  (reitit/router
   [["/" :index]
    ["/cards" {:name :card-list :area :cards}]
    ["/faqs" {:area :faqs}
     ["" :faq-list]
     ["/new" :faq-new]
     ["/search/:search-term" :faq-search]]
    ["/faq" {:area :faqs}
    ;;  TODO get these nested
     ["/:faq-id" :faq-view]
     ["/:faq-id/edit" :faq-edit]]
    ["/d3" {:area :d3}
     ["" :d3-overview]
     ["/bar-chart" :d3-bar-chart]
     ["/line-chart" :d3-line-chart]]]))

(defn route-for [route & [params]]
  (reitit/match-by-name router route params))

(defn path-for [route & [params]]
  (:path (route-for route params)))

(defn navigate!
  ([route] (navigate! route {}))
  ([route params] (accountant/navigate! (path-for route params))))
