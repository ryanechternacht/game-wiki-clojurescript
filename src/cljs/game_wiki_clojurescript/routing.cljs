(ns game-wiki-clojurescript.routing
  (:require [reitit.frontend :as reitit]
            [accountant.core :as accountant]
            [game-wiki-clojurescript.cards.views :as cards]
            [game-wiki-clojurescript.faqs.views :as faqs]))

;; -------------------------
;; Routes
(def router
  (reitit/router
   [["/" :index]
    ["/cards" {:name :card-list :area :cards}]
    ["/faqs" {:area :faqs}
     ["" :faq-list]
     ["/search/:search-term" :faq-search]]
    ["/faq" {:area :faqs}
     ["/:faq-id" :faq]]]))

(defn route-for [route & [params]]
  (reitit/match-by-name router route params))

(defn path-for [route & [params]]
  (:path (route-for route params)))

;; -------------------------
;; Translate routes -> page components
; Should these just be rolled into the router?
; I guess this way gives you a level of indirection?
; and the router technically exists w/o knowing anything of
; our codebase
; TODO why are these vars?
(defn page-for [route]
  (case route
    :card-list #'cards/cards-list-page
    :faq-list #'faqs/faq-list-page
    :faq #'faqs/faq-page
    :faq-search #'faqs/faq-search-page
    ""))

(defn navigate!
  ([route] (navigate! route {}))
  ([route params] (accountant/navigate! (path-for route params))))
