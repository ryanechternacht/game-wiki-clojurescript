(ns game-wiki-clojurescript.faqs.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [game-wiki-clojurescript.faqs.re-frame]))

(defn faq-list-page []
  (fn []
    [:div "Hello World - I'm the FAQ page"]))

(defn faq-page []
  (fn []
    [:div "I'm an individual FAQ page for " 5]))