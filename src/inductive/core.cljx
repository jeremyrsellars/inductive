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
        (def page-dom xom)))))

(-> logging-handler
    htmlparser.Parser.
    (.parseComplete "<div class=\"head1\">The <span class=\"hashem\">Lord</span>&#8217;s Message to Baruch</div><div class=\"noind\"><span class=\"chapter\">45<a id=\"bible.24.45.1\"/></span> This is the word that Jeremiah the prophet spoke to Baruch son of Neriah when he wrote these words on a scroll at Jeremiah&#8217;s dictationin the fourth year of Jehoiakim son of Josiah, king of Judah:&#160;<span class=\"verse\"><a id=\"bible.24.45.2\"/>2</span>&#160;&#8220;This is what the <span class=\"hashem\">Lord</span>, the God of Israel, says to you, Baruch: <span class=\"verse\"><a id=\"bible.24.45.3\"/>3</span>&#160;&#8216;You have said, &#8220;Woe is me, because the <span class=\"hashem\">Lord</span> has added misery to my pain! I am worn out with groaning and have found no rest.&#8221;&#160;&#8217;&#160;</div><div class=\"pNormal\"><span class=\"verse\"><a id=\"bible.24.45.4\"/>4</span>&#160;&#8220;This is what you are to say to him: &#8216;This is what the <span class=\"hashem\">Lord</span> says: What I have built I am about to demolish, and what I have planted I am about to uproot&#160;&#160;&#8212;&#160;the whole land! <span class=\"verse\"><a id=\"bible.24.45.5\"/>5</span>&#160;But as for you, do you seek great things for yourself? Stop seeking! For I am about to bring disaster on every living creature&#8217;&#160;&#8212;&#160;this is the <span class=\"hashem\">Lord</span>&#8217;s declaration&#160;&#8212;&#160;&#8216;but I will grant you your life like the spoils of war wherever you go.&#8217;&#160;&#8221;</div>"))


;; page-dom

(def app-state
  (atom
    {:text
     (js->clj page-dom :keywordize-keys true)

     :people
     [{:type :student :first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}]
     :classes
     {:6001 "The Structure and Interpretation of Computer Programs"}}))

(:text @app-state)

(defn text-view [app owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/div #js {:id "classes"}
        (map #(om/build container-view %) (:text app))))))


#+cljs
(defn- unescape-decimal [r]
  (js/String.fromCharCode (js/parseInt r 10)))
#+cljs
(defn- unescape-hex [r]
  (js/String.fromCharCode (js/parseInt r 16)))

(defn- unescape-html [html]
  (string/replace html
                  #"&(?:#(\d+)|(\d+));"
                  (fn [_ asc hex-asc] (cond (string? asc) (unescape-decimal asc)
                                            (string? hex-asc) (unescape-hex hex-asc)))))

(defn build-dom-for [elem]
  (let [fns-by-name {"div"  dom/div
                     "span" dom/span
                     "a"    dom/a}
        fns-by-type {"text" (fn [attrs children]
                              (let [raw-html (:data elem)
                                    data (unescape-html raw-html)]
                                (dom/span attrs data)))}
        dom-fn (or (get fns-by-type (:type elem))
                   (get fns-by-name (:name elem))
                   dom/span
                   )
        attribs    (:attribs elem)
        attributes (when attribs
                     (->> attribs
                          (map (fn [[k v]] [(if (= k :class) :className k) v]))
                          (into {})
                          clj->js))]
    (fn [attrs children]
      (apply dom-fn attributes (om/build-all container-view (:children elem))))))

(build-dom-for (:text @app-state))

(defn container-view [elem owner]
  (reify
    om/IRender
    (render [_]
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
