(ns game-wiki-clojurescript.faqs.views
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [reagent.core :as r]
            [reagent.session :as session]
            [re-frame.core :as rf]
            [game-wiki-clojurescript.faqs.re-frame]
            [game-wiki-clojurescript.routing :as routing]))

;; Helper funcs 
; Gets value of js event (for use in :on-change, etc.)
(def get-event-value #(-> % .-target .-value))

;; TODO clicking on popular tags doesn't cause this to update
;; the input
(defn search []
  (let [search-term (session/get-in [:route :route-params :search-term])
        val (r/atom search-term)]
    (fn []
      [:form.form-inline {:on-submit (fn [e]
                                       (do
                                         (.preventDefault e)
                                         (routing/navigate! :faq-search {:search-term @val})))}
       [:div
        [:input.mr-4.form-control {:placeholder "search" :default-value @val
                                   :on-change #(reset! val (get-event-value %))}]]
       [:button.btn.btn-primary "Search"]])))

(defn popular-tags []
  [:div.inline.small
   [:span.label "Popular Tags: "]
   (for [tag @(rf/subscribe [:faq-popular-tags])]
     ^{:key tag} [:a.ml-2 {:href (routing/path-for :faq-search {:search-term tag})} tag])])

; TODO figure out how to make this use child routes so the
; header doesn't need to be specified in every component
(defn header []
  [:div
   [:div.float-right
      ;; TODO hookup
    [:a.btn.btn-outline-primary {:href (routing/path-for :faq-new)} "New Entry"]]
   [search]
   [:div.mt-2
    [popular-tags]]])

(defn faq-list-page []
  [:div
   [header]
   [:hr]
   [:div "Hello World - I'm the FAQ page"]])

(defn faq-search-page []
  (let [search-term (session/get-in [:route :route-params :search-term])]
    [:div
     [header]
     [:hr]
     [:h2 (str "Results for " search-term)]
     [:ol
      (for [{:keys [id title]} @(rf/subscribe [:faq-search-results search-term])]
        ^{:key id}
        [:li
         [:a {:href (routing/path-for :faq-view {:faq-id id})} title]])]]))


;; not quite sure why it doesn't inherit the color from style, but w/e
(defn pencil-icon []
  [:svg.bi.bi-pencil {:width "1em" :height "1em" :view-box "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
   [:path {:fill-rule "evenodd" :clip-rule "evenodd" :d "M11.293 1.293a1 1 0 011.414 0l2 2a1 1 0 010 1.414l-9 9a1 1 0 01-.39.242l-3 1a1 1 0 01-1.266-1.265l1-3a1 1 0 01.242-.391l9-9zM12 2l2 2-9 9-3 1 1-3 9-9z"}]
   [:path {:fill-rule "evenodd" :clip-rule "evenodd" :d "M12.146 6.354l-2.5-2.5.708-.708 2.5 2.5-.707.708zM3 10v.5a.5.5 0 00.5.5H4v.5a.5.5 0 00.5.5H5v.5a.5.5 0 00.5.5H6v-1.5a.5.5 0 00-.5-.5H5v-.5a.5.5 0 00-.5-.5H3z"}]])

(defn faq-view-page []
  (let [faq-id (edn/read-string (session/get-in [:route :route-params :faq-id]))
        faq @(rf/subscribe [:faq faq-id])]
    [:div
     [header]
     [:hr]
     [:div.float-right
      ;; TODO implement
      [:a.btn.btn-outline-primary {:href (routing/path-for :faq-edit {:faq-id faq-id})}
       [pencil-icon]]]
     [:h2 (:title faq)]
     [:h4 "Tags"]
     [:ul
      (for [tag (:tags faq)]
        ^{:key tag} [:li tag])]
     [:h4 "Body"]
     [:span {:dangerouslySetInnerHTML {:__html (:body faq)}}]]))

(defn- convert-tags-to-vec [tags]
  (str/split tags #"[\s,]+"))

(defn faq-edit-page []
  (let [faq-id (edn/read-string (session/get-in [:route :route-params :faq-id]))
        form (r/atom @(rf/subscribe [:faq faq-id]))]
    (fn []
      [:div
       [header]
       [:hr]
  ;; TODO should wait for save confirmation before going back
       [:form {:on-submit (fn [e]
                            (do
                              (.preventDefault e)
                              (rf/dispatch [:save-faq @form])
                              (routing/navigate! :faq-view {:faq-id faq-id})))}
        [:div.form-group
         [:label {:for "title"} "Title"]
         [:input#title.form-control {:default-value (:title @form)
                                     :on-change (fn [e] (swap! form #(assoc % :title (get-event-value e))))}]]
   ;;TODO we don't have the fancy tags component for this
        [:div.form-group
         [:label {:for "tags"} "Tags"]
         [:input#tags.form-control {:default-value (str/join ", " (:tags @form))
                                    :on-blur (fn [e] (swap! form #(assoc % :tags (->> e
                                                                                      get-event-value
                                                                                      convert-tags-to-vec))))}]]
   ;;TODO This should be tinymce
        [:div.form-group
         [:label {:for "body"} "Body"]
         [:textarea#body.form-control {:default-value (:body @form)
                                       :on-change (fn [e] (swap! form #(assoc % :body (get-event-value e))))}]]
        [:div {} @form]
        [:button.btn.btn-primary.mt-4 "Save"]]])))
