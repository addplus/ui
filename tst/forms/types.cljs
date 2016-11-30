(ns forms.types
  (:require
    [cljs.pprint :as pp]
    [javelin.core :as j :refer [defc defc= cell= cell cell-let with-let]]
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [hoplon.ui :as hui :refer [window elem bind-in!
                               node parse-args destyle line-field vstr]]
    [hoplon.ui.plus :as hui+ :refer [row container]]
    [hoplon.ui.attrs :refer [c r d em px]]
    [hoplon.ui.elems :refer [box doc out mid in elem?]]))

(defelem form [attrs elems] (elem attrs elems))
(defelem line [attrs elems]
  (elem :sh (em 20) :b 1 :bc (c 0 0.1) :p 4 "<line>" attrs
        elems))

(defelem write [attrs elems] (elem :c (c 0 0.3) :p 4 attrs elems))

(defn lines []
  )

(defn pick []
  )

(defn picks []
  )

(defn toggle []
  )

(defn page []
  (let [defaults {}
        data (cell defaults)
        model {:data data}
        parse identity
        render str]
    (window
      :title "Different types of form controls" :p (em 1)
      (form
        :model model
        (line)                                              ; Defaults to :type :str
        (line :type :str)
        (line :type :int)
        (line :type :float)
        (line :type [parse render])
        (lines)
        (pick)
        ; Radio group
        ; Single Select
        ; Slider
        (picks)
        ; Checkbox group
        ; Multi Select
        (toggle)                                            ; Boolean pick
        ; Checkbox
        ; Slide switch
        (write))
      (elem :sh (r 1 1)
            (cell= (str "Form model: " data))))))
