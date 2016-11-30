(ns hoplon.sandbox
  (:require
    [cljs.pprint :as pp]
    [javelin.core :as j :refer [defc defc= cell= cell cell-let with-let]]
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [hoplon.ui :as hui :refer [window elem bind-in!
                               node parse-args destyle line-field vstr]]
    [hoplon.ui.plus :as hui+ :refer [row container]]
    [hoplon.ui.attrs :refer [c r d em px]]
    [hoplon.ui.elems :refer [box doc out mid in elem?]]))

(defc x true)

(defn page []
  (window
    :title "Sandbox" :p (em 1) :g 8
    (row (elem "Hello"
               :p 8
               :bc (cell= (if x (c 0xFF0000 0.3) (c 0x00FF00 0.3)))
               :b 1 #_(cell= (if x 1 10))))
    (row (elem "Flick"
               :click #(swap! x not)
               :p 8 :c (c 0 0.3)))))
