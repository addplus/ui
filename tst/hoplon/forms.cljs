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
(def p 4)
(def db {:b 1 :bc (c 255 0 0 0.1)})
(defelem row [a e] (elem :sh (r 1 1) a e))
(defelem label [a e] (elem :ah :end a e))
(defelem line+ [a e] (hui/line+ :f 12 :p p :b 1 :bc transparent-grey a e))
(defelem select [a e] (hui/select :p p :b 1 :bc transparent-grey a e))
(defelem radio [{:keys [key val] :as attrs} elems]
  (row :gh g (dissoc attrs :key :val)
       (hui/radio :s 14 :key key :val val)
       elems))

(defn path-cell [c path & [not-found]]
  (cell= (get-in c path not-found) (partial swap! c assoc-in path)))

(defc form1-default {:amt "123"
                     :select {:single :option-1}})

#_(h/with-timeout 3000
  (reset! form1-default {:amt "999"
                         :select {:single :option-2}}))

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
                :p p
                :ah :mid
                :c transparent-grey
                :submit #(hash-map :amt "<reset>"
                                   :select {:single #{'sym-opt}}
                                   :animal/type :animal.type/bat))

    ;(row "Single select")
    (select :sh (r 1 1)
            :key [:select :single]
            (h/option :value "" "--- Select something ---")
            (h/option :value :kw-opt "Keyword Option")
            (h/option :value 'sym-opt "Symbol Option")
            (h/option :value (pr-str "str-opt") "String Option")
            (h/option :value "\"str-opt-2\"" "Other String Option"))

    ;(row "Multi-select")
    (select :sh (r 1 1)
            :sv (em 4)
            :key [:select :multiple]
            :multi? true
            (h/option :value "" "--- Select something ---")
            (h/option :value :kw-opt "Keyword Option")
            (h/option :value 'sym-opt "Symbol Option")
            (h/option :value (pr-str "str-opt") "String Option")
            (h/option :value "\"str-opt-2\"" "Other String Option"))

    ;(row "Radio buttons")
    (radio :key :animal/type :val :animal.type/cat "Cat")
    (radio :key :animal/type :val :animal.type/dog "Dog")
    (radio :key :animal/type :val :animal.type/bat "Bat")))

(defn page []
  (window
    (row :ah :mid
         (form1))))
