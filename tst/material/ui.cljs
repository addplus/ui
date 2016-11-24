(ns material.ui
  (:refer-clojure :exclude [+ - * /])
  (:require
    [javelin.core :as j :refer [cell cell= defc defc= cell-let with-let]]
    [hoplon.core :as h :refer [defelem when-tpl if-tpl case-tpl for-tpl]]
    [hoplon.ui :as hui :refer [elem b bind-in! cmpt destyle node parse-args
                               line-field
                               fieldable+
                               send-field+]]
    [hoplon.ui.attrs :refer [+ - * / c r pt em px d]]
    [hoplon.ui.elems :refer [box doc out mid in elem?]]
    [material.colors :as c]
    [material.spinner-html :as spinner-html])
  (:require-macros
    [material.ui]))

; Sources:
;   https://themeforest.net/item/triangular-material-design-admin-template-angularjs/11711437
;   http://demo.oxygenna.com/triangular

;;; styles ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def f 21)
(def p 18)

; add+ brand colors
(def c1 "#000")
(def c2 "rgb(149,139,137)")
(def c3 "rgb(168,175,170)")
(def c4 "rgb(190,190,171)")
(def c5 "rgb(142,130,107)")
(def c6 "rgba(149,139,137,0.2)")

; fonts
(def roboto ["Roboto" :sans-serif])

(def font-black (c 0x333333))

(def font-height 21)

;;; styles ;;;

; breakpoints
(def mobile 500)
(def sm 760)
(def md 1240)
(def lg 1480)

; sizes

(def pad-sm 8)
(def pad-md 16)
(def pad-lg 24)
(def pad-xl 32)

(def g-sm 8)                                                ;standard
(def g-md 12)
(def g-lg 16)

(def title 80)
(def toolbar 56)
(def subtitle 48)
(def f-sm 11)
(def f-md 14)
(def f-lg 18)
(def f-xl 24)

(def icon-sm 18)
(def icon-md 24)                                            ;standard
(def icon-lg 36)

(def form-width 560)

(def uimap
  {:title         {:ff "Roboto" :f f-xl :fc c/black :ft :300}
   :subtitle      {:ff "Roboto" :f f-sm :fc c/grey-500}
   :tab           {:ff "Roboto" :f f-md :fc c/black}
   :card-title    {:ff "Roboto" :f f-lg :fc c/black}
   :card-subtitle {:ff "Roboto" :f f-md :fc c/grey-500}
   :footer        {:ff "Roboto" :f f-sm :fc c/black}})

;button-types
(def primary
  {:b  1
   :bc c/accent-color
   :c  c/accent-color
   :fc c/white
   :av :mid
   :f  f-md :sv icon-lg :ph pad-md})

(def secondary
  {:b  1
   :bc c/accent-color
   :c  c/white
   :fc c/accent-color})

(def alert
  {:b  1
   :bc c/red-400
   :c  c/red-400
   :fc c/white})

(defn ui [type]
  (uimap type))

(defelem icon [attrs elems]
  (elem :f icon-md :fh icon-sm :ff "Material Icons" :m :pointer :fr :optimizeLegibility
        attrs elems))

(defelem fa-icon [attrs elems]
  (icon :f icon-sm :ff "FontAwesome" :ph pad-sm attrs elems))

(defn asterisk []
  (elem :fc c/accent-color :pr 2 "*"))

(defelem pre [a e]
  (with-let [e' (elem a e)]
    (bind-in! e' [in .-style .-whiteSpace] "pre-wrap")))

;box-shadow: [offset left/right] [offset top/bottom] [color] & [blur] [spread] inset;
;https://web.archive.org/web/20140627054059/http://www.google.com/design/spec/layout/layout-principles.html#layout-principles-dimensionality
(defn paper-shadow [depth]
  (case depth
    1 [(d 0 (px 1) (c 0 0 0 0.12) 1.5)
       (d 0 (px 1) (c 0 0 0 0.24) 1)]
    2 [(d 0 (px 3) (c 0 0 0 0.16) 3)
       (d 0 (px 3) (c 0 0 0 0.23) 3)]
    3 [(d 0 (px 10) (c 0 0 0 0.19) 10)
       (d 0 (px 6) (c 0 0 0 0.23) 3)]
    4 [(d 0 (px 14) (c 0 0 0 0.25) 45)
       (d 0 (px 10) (c 0 0 0 0.22) 5)]
    5 [(d 0 (px 19) (c 0 0 0 0.30) 19)
       (d 0 (px 15) (c 0 0 0 0.22) 6)]
    [(d 0 (px 1) (c 0 0 0 0.12) 1.5)
     (d 0 (px 1) (c 0 0 0 0.24) 1)]))

(defn bg-img* [ctor]
  "Background image"
  (fn [{:keys [background] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :background) elems)]
      (bind-in! e [mid .-style .-background] background))))

(def section (-> h/div box node bg-img* parse-args))

(defelem row [attrs elems]
  (elem :sh (r 1 1) attrs elems))

(defelem radio [{:keys [key val] :as attrs} [label-content]]
  (hui/label+ :sh (r 1 1) :gh g-lg :av :mid (dissoc attrs :key :val)
              (hui/radio+ :s 14 :key key :val val)
              label-content))

(defelem checkbox [{:keys [key val] :as attrs} elems]
  (row :gh g-lg (dissoc attrs :key :val)
       (elem :sv (r 1 1) :sh 14 :av :beg
             (hui/toggle :key key :val val))
       (elem :sv (r 1 1) :sh (r 9 11) elems)))

(defelem select-minimal [attrs elems]
  (hui/select+
    :fc c/dark-primary-color
    :bb 1 :bc c/divider-color
    attrs
    elems))

(defelem select [attrs elems]
  (hui/select+
    :sh (r 1 1) :p 7
    :f f-md :fc c/dark-primary-color
    :b 1 :bc c/divider-color
    attrs
    elems))

(defelem container [attrs elems]
  (elem :s (r 1 1) attrs elems))

(defelem paper [{:keys [depth] :as attrs} elems]
  (row :p pad-sm :g pad-sm
       :d (cell= (paper-shadow depth)) (dissoc attrs :depth)
       elems))

(defelem card
  [{:keys [open? title subtitle openable commentable image*] :as attrs} elems]
  "Essentially paper with a title, subtitle and abilty to hide"
  (let [default-open (or open? false)
        current-open (cell false)
        expand? (cell= (or current-open default-open))
        expand-fn (fn [] (reset! current-open true))
        collapse-fn (fn [] (reset! current-open false))]
    (paper
      :g 0
      (dissoc attrs :open? :title :subtitle :openable :commentable :image*)
      (when-tpl (or title subtitle)
        (container :sh (r 1 1) :sv (cell= (if title (if subtitle 52 24) 0)) :pv pad-sm :pl pad-md :c c/light-primary-color
          (container :sh (cell= (if (or openable commentable image*) (r 4 5) (r 1 1))) :sv (r 1 1) :av :mid :ah :beg
            (row (ui :card-title) :fc c/dark-primary-color title)
            (when-tpl subtitle (elem :sh (r 1 2) (ui :card-subtitle) subtitle)))
          (container :sh (cell= (if (or openable commentable image*) (r 1 5) (r 1 1))) :sv (r 1 1) :av :mid :ah :end
            :pr pad-lg
            (when-tpl openable
              (container
                (if-tpl (cell= (true? expand?))
                        (icon "keyboard_arrow_up" :click #(collapse-fn))
                        (icon "keyboard_arrow_down" :click #(expand-fn)))))
            (when-tpl commentable (icon "comment"))
            (when-tpl image*
              (section :sv 40 :sh 40 :r 20 :b 1 :bc c/grey-500 :background image*))
            )))
      (when-tpl elems
        (row :sv (cell= (if (or title subtitle) (- (r 1 1) 30) (r 1 1)))
             (row :sv (cell= (if expand? (r 4 5) (r 1 1))) :ff roboto :f f-md elems)
             (if-tpl (cell= (true? expand?))
                     (row :sv (r 1 5) :g g-sm
                          "expanded
                            yo
                            yoy
                            yoyoyo
                            yoyoyo")))))))

(defelem feature-box [{:keys [title] :as attrs} elems]
  (elem :sh (b :sh (r 1 3) sm 100) (dissoc attrs :title)
        (row :ph 3 :av :mid
             title)
        (row :sv (- (r 1 1) 26) :f 30
             elems)))

; http://blog.w3conversions.com/2011/09/css3-spread-value-and-box-shadow-on-one-side-only/
(defelem view-title [{:keys [title rhs] :as attrs} elems]
  (row :sv subtitle :bb 2 :bc c/theme-flash-color
       :d (d 0 6 (c 0 0 0 0.23) 3 -3)
       (dissoc attrs :title :rhs)
       (elem :sh (r 1 3) :sv (r 1 1) :av :beg :f f-xl :ft :300 :fc c/black
             title)
       (elem :sh (r 2 3) :sv (r 1 1) :ah :end :g g-sm :av :mid
             elems)))

;;; components ;;;

(defelem field [attrs elems]
  (hui/line+ :sh (r 1 1) :sv 36
             :ph pad-sm
             :b 1 :bc c/divider-color
             :f f-md
             attrs elems))

(defn label-field-group [key label & {:as attrs}]
  (cmpt :g g-sm :sh (r 1 1) :av :mid
        (elem :sh (r 1 1) label)
        (field :key key
               attrs)))

(defn required-field-group [key label & {:as attrs}]
  (cmpt :gv g-sm :sh (r 1 1)
        (elem (asterisk) label)
        (field :key key
               attrs)))

(defn one-line-field-group [key label & {:keys [icon*] :as attrs}]
  (row :g g-sm :av :mid
       (elem :sh (r 1 3) :ah :end :gh g-sm :ft :300 :av :mid
             label
             (icon :fc c/dark-primary-color
                   icon*))
       (row :sh (cell= (- (r 2 3) 32))
            (field :key key
                   (dissoc attrs :icon*)))))

(defn error-msg [error-cell]
  (let [msg (cell= (and error-cell (.-message error-cell)))]
    (when-tpl (cell= (seq msg))
      (elem :sh form-width :c c/contrast-color
            :fc c/white
            :xt 60 :p pad-sm :d (paper-shadow 3) :r 4
            msg))))

;;; elements ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defelem action-button [attrs elems]
  (cmpt :s 56 :ah :mid :av :mid :r 56 :m :pointer
        :c c/blue-400 :fc c/white :d (cell= (paper-shadow 3))
        attrs elems))

(defelem flat-button [attrs elems]
  (cmpt :m :pointer :f f-md :sv 36 :ph 16 :av :mid :b 1 :bc c/grey-600 :c c/grey-100
        attrs elems))

(defelem raised-button [attrs elems]
  (cmpt :m :pointer :d (cell= (paper-shadow 2)) :f f-md :sv 36 :ph 16 :av :mid :b 1 :bc c/grey-600 :c c/grey-100
        attrs elems))

(defelem submit [attrs elems]
  (hui/write+ :m :pointer primary attrs elems))

(defelem field-group [attrs elems]
  (cmpt :pv pad-sm :gv g-sm :sh (r 1 1) attrs elems))

(defn href [ref]
  (set! js/window.location.href ref))

(defn anchor [& [url]]
  (merge {:m :pointer}
         (when url {:click #(href url)})))

(defn link-new [label url]
  (elem :fc c/accent-color :fu :underline :m :pointer
        :click #(js/window.open url "_blank")
        label))

(defn html* [ctor]
  "Raw HTML source for the inner element"
  (fn [{:keys [html] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :html) elems)]
      (bind-in! e [in .-innerHTML] html))))

(def html (-> h/div box node html* parse-args))

(defelem markdown [{:keys [md] :as attrs} elems]
  (html :f f-md :ft :300
        :html (cell= (-> {:html true}
                         (clj->js)
                         (js/markdownit.)
                         (.render (or md ""))))
        (dissoc attrs :md)))

(defelem spinner
  "Material Design Spinner from https://codepen.io/mrrocks/pen/EiplA"
  [attrs elems]
  (html :sh (r 1 1) :sv (r 1 1) :ah :mid :av :mid attrs
        :html spinner-html/mr-rocks))

(defelem message-spinner [{:keys [message] :as attrs} elems]
  (container :ah :mid :av :mid (dissoc attrs :message)
    (spinner)
    (elem :f f-md message)))

(defelem loading-io-spinner
  "From http://loading.io/"
  [attrs elems]
  (html attrs :html spinner-html/loading-io))

(defn tabs
  "Tab container

   Takes a map of tabs, where key is Tab Name and value the element use as the tab content.
   Also accepts keyword arguments:
   * :default - the default tab table to be selected
   * :current - a cell with the tab title that should be focused. nil uses the default
   * :change  - a callback with one argument (tab title) that should be called on tab click.
  "
  [children & {:keys [default
                      change
                      callback
                      current
                      attrs-bar
                      attrs-tab
                      attrs-selected-tab
                      attrs-content
                      bar-height
                      tab-width]
               :or   {attrs-bar          {}
                      attrs-tab          {:fc c/secondary-text}
                      attrs-selected-tab {:fc c/theme-flash-color :ft :bold}
                      attrs-content      {}
                      bar-height         32
                      tab-width          140}}]
  (let [default-tab (or default (first children))
        current-tab (or current (cell nil))
        selected-tab (cell= (or current-tab default-tab))
        swap-fn (or change #(reset! current-tab %))         ; To swap tabs
        change-fn #(do (swap-fn %)
                       (when callback (callback %)))        ; also calls callback if specified
        splitkids (partition 3 children)]
    (container
      (row :sv bar-height attrs-bar
           (for [[id title _] splitkids]
             (let [base-options {:sv (r 1 1)
                                 :m  :pointer :click #(change-fn id)
                                 :av :mid :ah :mid
                                 :bc c/theme-flash-color :bb nil
                                 :ph pad-md
                                 :f  f-md
                                 :fc c/secondary-text}]
               (if-tpl (cell= (= selected-tab id))
                       (elem :sv (r 1 1) (merge base-options attrs-tab attrs-selected-tab) title)
                       (elem :sv (r 1 1) (merge base-options attrs-tab) title)))))
      (elem :bt 1 :bc c/theme-flash-color :sh (r 1 1) :sv (- (r 1 1) bar-height) attrs-content
            (for [[id _ content] splitkids]
              (when-tpl (cell= (= selected-tab id))
                (container :f f-md (content))))))))

(defelem dialog
  "Use :sv+ \"80vh\" to limit dialog height to the viewport
   and allow scrolling of longer content."
  [{:keys [title open? close! sv+] :as attrs} elems]
  (when-tpl open?
    (container :xt 0 :xl 0
      :c (c 0 0 0 0.5)
      :a :mid
      (with-let
        [e (paper :depth 5 :c c/white :sh (r 1 2) :pb pad-lg
                  (dissoc attrs :open? :close! :title :sv+)
                  (when-tpl open?
                    (container
                      (row :ah :end (icon "close"
                                          :click close!
                                          :sh (r 1 16) :ah :end))
                      (container :g g-sm :ph pad-md
                        (row :sh (r 1 1) :ah :mid title :f (em 2))
                        elems))))]
        (bind-in! e [in .-style .-maxHeight] sv+)
        (bind-in! e [in .-style .-overflowY] "auto")))))

(defelem hr [attrs _]
  (markdown :sh (r 1 1) attrs :md "----"))
