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
(defelem line [a e] (hui/line :f 12 :p 4 :b 1 :bc transparent-grey a e))

(defn path-cell [c path & [not-found]]
  (cell= (get-in c path not-found) (partial swap! c assoc-in path)))

(def form1-default-data
  {:amt   "123"
   :email "someone@example.com"
   :pwd   "asd"})

(defc form1-data form1-default-data)

(defn form1 []
  (let [amt (path-cell form1-data [:amt])
        email (path-cell form1-data [:email])
        pwd (path-cell form1-data [:pwd])]
    (hui/form
      :g g :av :mid
      :submit #(do
                 (js/console.debug "form1-data" @form1-data)
                 (reset! form1-data form1-default-data))
      (label :sh (r 1 2) "Amount:")
      (line :sh (r 1 2)
            :val amt)
      (label :sh (r 1 2) "Email:")
      (line :sh (r 1 2)
            :val email)
      (label :sh (r 1 2) "Password:")
      (line :sh (r 1 2)
            :val pwd
            :content :password))))

(defn page []
  (window
    (row :ah :mid
         (form1))))
