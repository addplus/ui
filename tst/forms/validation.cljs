(ns forms.validation
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

(defelem write [attrs elems]
  )

; TODO Form level validation
; TODO Field level validation
; TODO Error message list calculation
(defn page []
  (let [defaults {}
        data (cell defaults)
        errors (cell {:email #{:required}})

        any-error? (cell= (seq errors))
        m {:data   data
           :errors errors}]
    (window
      :title "Form validation" :p (em 1)

      ; Error summary
      (when-tpl any-error?
        (row (cell= (str errors))))

      (form
        :model m
        :g 8
        (row "Name: " (line :model m :key :name))

        (row "Email: " (line :model m :key :email :req true
                             (cell= (if (:email errors)
                                      {:b 1 :bc (c 0xFF0000 0.3)}
                                      {:bc (c 0x00FF00 0.3)}))))

        (cell-let [{:keys [email]} errors]
          (when-tpl email
            (row (cell= (str email)))))

        (write :model m))
      (elem :sh (r 1 1)
            (cell= (str "Form m: " data))))))
