(ns game-wiki-clojurescript.handler
  (:require
   [reitit.ring :as reitit-ring]
   [game-wiki-clojurescript.middleware :refer [middleware]]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:div {:style "position: relative; background-color: #007bff !important; display: flex;
                  width: 100%; padding: .5rem 1rem; align-items: center"}
    [:div {:style "color: white; padding: 0.3125rem 0; font-size: 1.25rem; display: inline-block;
                   line-height: inherit"} "Game Wiki"]]
   [:div {:style "max-width: 540px; width: 100%; padding: 0 15px; margin: 0 auto;"}
    [:h5 {:style "margin-top: 20px;"} "One moment please..."]
    [:h5 "We're getting everything ready just for you!"]]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))
   ;; Bootstrap
   ;; TODO pull these from somewhere more reasonable (npm?)
   (include-js "https://code.jquery.com/jquery-3.4.1.slim.min.js")
   (include-js "https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js")
   (include-css "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css")
   (include-js "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js")])

(defn loading-page []
  (html5
   (head)
   [:body
    mount-target
    (include-js "/js/app.js")]))


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

;;TODO can these routes be reduced into a catch-all?
(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     ["/cards" {:get {:handler index-handler}}]
     ["/faqs"
      ["" {:get {:handler index-handler}}]
      ["/search/:search-term" {:get {:handler index-handler}}]]
     ["/faq"
      ["/:faq-id"
       ["" {:get {:handler index-handler}}]
       ["/edit" {:handler index-handler}]]]
     ["/d3"
      ["" {:get {:handler index-handler}}]
      ["/bar-chart" {:get {:handler index-handler}}]
      ["/bar-chart-v2" {:get {:handler index-handler}}]
      ["/line-chart" {:get {:handler index-handler}}]
      ["/line-chart-v2" {:get {:handler index-handler}}]
      ["/line-chart-v3" {:get {:handler index-handler}}]
      ["/tetherball-chart" {:get {:handler index-handler}}]]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))
