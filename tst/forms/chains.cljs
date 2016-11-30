(ns forms.chains
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

(defelem form-step-1 [{:keys [model] :as attrs} elems]
  (form attrs :g 8
        (row "Form 1")
        (row (line :model model :key :name))
        (write "Next >>"
               :click #(reset! (:step model) ::step2))))

(defelem form-step-2 [{:keys [model] :as attrs} elems]
  (form attrs :g 8
        (row "Form 2")
        (row (line :model model :key :email))
        (write "<< Back"
               :click #(reset! (:step model) ::step1))
        (write "Submit"
               :click #(reset! (:step model) ::done))))

(defn page []
  "Pressing Enter in any of the form controls should call
  a submit action which is specified at the form level once."
  (let [defaults {:name  "Hello Kitty"
                  :email "hello@kitty.com"}
        data (cell defaults)
        step (cell nil)
        model {:data data
               :step step}
        submitted (cell {:submitted "data"})]
    (window
      :title "Form default submit action" :p (em 1)
      (case-tpl step
        (::step1 nil) (form-step-1 :model model)
        ::step2 (form-step-2 :model model)
        ::done (row (cell= (str "Form submitted: " submitted))))
      (elem :sh (r 1 1)
            (cell= (str "Form model: " data))))))

; TODO Restrict upcoming steps if previous steps are not validated yet.