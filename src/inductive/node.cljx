(ns inductive.node
  (:require [inductive.core :as core]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string]
            #+node [cljs.nodejs :as nodejs]
            ))


;(nodejs/enable-util-print!)


#+node (defn ^export -main []
   (set! module.exports.getContentHtml (fn []
     (dom/render-to-str (om/build core/registry-view @core/app-state)))))

#+node (-main)

#+node (set! *main-cli-fn* -main)
