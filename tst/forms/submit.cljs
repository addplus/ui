(ns forms.submit
  (:require
    [cljs.pprint :as pp]
    [javelin.core :as j :refer [defc defc= cell= cell cell-let with-let]]
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [hoplon.ui :as hui :refer [window elem bind-in!
                               node parse-args destyle line-field vstr]]
    [hoplon.ui.plus :as hui+]
    [hoplon.ui.attrs :refer [c r d em px]]
    [hoplon.ui.elems :refer [box doc out mid in elem?]]))

(defelem form [attrs elems] (elem attrs elems))
(defelem line [attrs elems] (elem attrs "<line>" elems))

(defelem write [attrs elems] (elem attrs elems))

(defn page []
  "Pressing Enter in any of the form controls should call
  a submit action which is specified at the form level once."
  (let [defaults {:name "Hello Kitty"}
        {:keys [data] :as fm} {:data (cell defaults)}
        action (partial js/console.debug "Submitted data:")]
    (window
      :title "Form default submit action" :p (em 1)
      (form
        :model fm
        :submit action
        :g 8
        (line :model fm
              :key :name
              :sh (em 20) :b 1 :bc (c 0 0.1) :p 4)
        (write "Submit")
        (elem :sh (r 1 1)
              (cell= (str "Form model: " data)))))))