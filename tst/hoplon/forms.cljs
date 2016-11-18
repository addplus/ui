(ns hoplon.forms
  (:require
    [javelin.core :as j :refer [defc defc= cell= cell cell-let]]
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [hoplon.ui :as hui :refer [window elem]]
    [hoplon.ui.attrs :refer [c r d em px]]))

(def grey (c 0x888888))
(def transparent-grey (c 128 128 128 0.5))
(def black (c 0x1F1F1F))
(def orange (c 0xE73624))
(def blue (c 0x009BFF))
(def yellow (c 0xF5841F))
(def g 12)
(def db {:b 1 :bc (c 255 0 0 0.1)})
(defelem row [a e] (elem :sh (r 1 1) :g g a e))
(defelem label [a e] (elem :ah :end a e))
(defelem line+ [a e] (hui/line+ :f 12 :p 4 :b 1 :bc transparent-grey a e))
(defelem select [a e] (hui/select :p 4 :b 1 :bc transparent-grey a e))

(defn path-cell [c path & [not-found]]
  (cell= (get-in c path not-found) (partial swap! c assoc-in path)))

(defc form1-default {:amt "123"
                     :lvl1 {:lvl2 :option-1}})

#_(h/with-timeout 3000
  (reset! form1-default {:amt "999"
                         :lvl1 {:lvl2 :option-2}}))

(defn form1 []
  (hui/form+
    :g g :av :mid
    :default form1-default
    :submit #(do
               (js/console.debug "form1-data" %)
               {:amt "<submission>"})
    :change #(js/console.info "form change" %)
    (label :sh (r 1 2) "Amount:")
    (line+ :sh (r 1 2)
           :key :amt
           :autofocus true)
    (hui/write+ :label "Submit & Reset"
                :sh (r 1 1)
                :ah :mid
                :c transparent-grey
                :submit #(reset! hui/*data* {:amt  "<reset>"
                                             :lvl1 {:lvl2 'sym-opt}}))
    (select :sh (r 1 1)
            :sv (em 4)
            :key [:lvl1 :lvl2]
            :multi? true
            (h/option :value "" "--- Select something ---")
            (h/option :value :kw-opt "Keyword Option")
            (h/option :value 'sym-opt "Symbol Option")
            (h/option :value (pr-str "str-opt") "String Option")
            (h/option :value "\"str-opt-2\"" "Other String Option"))))

(defn page []
  (window
    (row :ah :mid
         (form1))))
