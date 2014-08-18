(ns inductive.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string]
            ))

(enable-console-print!)

(def htmlparser
  #+node (node/require "htmlparser")
  #+browser Tautologistics.NodeHtmlParser)


;;(.log js/console "wazza!")

(def logging-handler
 (htmlparser.DefaultHandler.
  (fn [err xom]
    (do (when err
          (js/alert err)
          (.log js/console err))
        (.log js/console xom)
        (def page-dom xom)))))

(-> logging-handler
    htmlparser.Parser.
    (.parseComplete "<div class=\"head1\">The <span class=\"hashem\">Lord</span>&#8217;s Message to Baruch</div>"))


;; page-dom

(def app-state
  (atom
    {:text
     (-> page-dom
         (js->clj :keywordize-keys true)
          first)

     :people
     [{:type :student :first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}]
     :classes
     {:6001 "The Structure and Interpretation of Computer Programs"}}))

(:text @app-state)

(defn text-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "classes"}
        (#(om/build container-view %) (:text app))))))


(defn build-dom-for [elem]
  (let [_ (.log js/console "elem-blem")
        _ (.log js/console elem)
        fns-by-name {"div"  dom/div
                     "span" dom/span}
        fns-by-type {"text" (fn [attrs children]
                              (.log js/console "elem" (str elem) "attrs" (str attrs) "children" (str children))
                              (dom/span attrs (str (:data elem))))}
        dom-fn (or (get fns-by-type (:type elem))
                   (get fns-by-name (:name elem))
                   ;dom/span
                   )
        attributes (->> (:attribs elem)
                        (map (fn [[k v]] [(if (= k :class) :className k) v]))
                        (into {})
                        clj->js)]
    (fn [attrs children]
      (apply dom-fn attributes (om/build-all container-view (:children elem))))))

(build-dom-for (:text @app-state))

(defn container-view [elem owner]
  (reify
    om/IRender
    (render [_]
            (let [_ (.log js/console (str elem))])
      (apply (build-dom-for elem) {:className (-> elem :attribs :class)}
        (map #((build-dom-for %) nil (str (:data %))) (:children elem))))))

#+node (defn middle-name [{:keys [middle middle-initial]}]
  (cond
    middle (str " " middle)
    middle-initial (str " " middle-initial ".")))

#+node (defn display-name [{:keys [first last] :as contact}]
  (str last ", " first (middle-name contact)))

#+node (defn people [app]
  (->> (:people app)
    (mapv (fn [x]
            (if (:classes x)
              (update-in x [:classes]
                (fn [cs] (mapv (:classes app) cs)))
               x)))))

#+node
(defmulti entry-view (fn [person _] (:type person)))

#+node
(defn student-view [student owner]
  (reify
    om/IRender
    (render [_]
      (dom/li nil (display-name student)))))

#+node
(defmethod entry-view :student
  [person owner] (student-view person owner))

#+node
(defn registry-view [app owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:id "registry"}
        (dom/h2 nil "Registry")
        (apply dom/ul nil
          (om/build-all entry-view (people app)))))))
#+node
(defn classes-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "classes"}
        (dom/h2 nil "Classes")
        (apply dom/ul nil
          (map #(dom/li nil %) (vals (:classes app))))))))

(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))
(defn handle-change [e text owner]
  (om/transact! text (fn [_] (.. e -target -value))))
(defn commit-change [text owner]
  (om/set-state! owner :editing false))
(extend-type js/String
  ICloneable
  (-clone [s] (js/String. s))
  om/IValue
  (-value [s] (str s)))

#+node
(defn editable [text owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ {:keys [editing]}]
      (dom/li nil
        (dom/span #js {:style (display (not editing))} (om/value text))
        (dom/input
          #js {:style (display editing)
               :value (om/value text)
               :onChange #(handle-change % text owner)
               :onKeyPress #(when (== (.-keyCode %) 13)
                              (commit-change text owner))
               :onBlur (fn [e] (commit-change text owner))})
        (dom/button
          #js {:style (display (not editing))
               :onClick #(om/set-state! owner :editing true)}
          "Edit")))))

#+node
(defn professor-view [professor owner]
  (reify
    om/IRender
    (render [_]
      (dom/li nil
        (dom/div nil (display-name professor))
        (dom/label nil "Classes")
        (apply dom/ul nil
          (map #(dom/li nil (om/value %)) (:classes professor)))))))

#+node
(defn classes-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "classes"}
        (dom/h2 nil "Classes")
        (apply dom/ul nil
          (map #(om/build editable %) (vals (:classes app))))))))

#+node
(defmethod entry-view :professor
  [person owner] (professor-view person owner))


(defn root-content []
  (om/root text-view app-state
    {:target (. js/document (getElementById "content"))}))


(root-content)
;; (def jer45 "http://localhost:24455/hcsb/HCSB-24_Jer_45_nonotes.xhtml")

;; (when-not js/require
;;   (xhr/send jer45 (fn [reply] (.log js/console (.getResponse (.-target reply))))))


;; (let [api-url jer45
;;             ;; initialize ajax client
;;             xhr (gnet/xhr-connection.)]
;;         ;; register handlers
;;         (gevent/listen xhr :error #(.log js/console "Error" %1))
;;         (gevent/listen xhr :success #(.log js/console "Suceess" %1))
;;         ;; make request
;;         (gnet/transmit xhr api-url "GET" {:q "json"}))
