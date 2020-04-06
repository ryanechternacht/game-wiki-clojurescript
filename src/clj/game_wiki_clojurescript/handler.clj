(ns game-wiki-clojurescript.handler
  (:require
   [reitit.ring :as reitit-ring]
   [game-wiki-clojurescript.middleware :refer [middleware]]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:h2 "Welcome to game-wiki-clojurescript"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

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
      ["line-chart" {:get {:handler index-handler}}]]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))
