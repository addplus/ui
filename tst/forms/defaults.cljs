(ns forms.defaults
  (:require
    [cljs.pprint :as pp]
    [javelin.core :as j :refer [defc defc= cell= cell cell-let with-let]]
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [hoplon.ui :as hui :refer [window elem bind-in!
                               node parse-args destyle line-field vstr]]
    [hoplon.ui.attrs :refer [c r d em px]]
    [hoplon.ui.elems :refer [box doc out mid in elem?]]))

(defelem form [attrs elems]
  (elem (dissoc attrs :model) elems))

(defn fieldable [ctor]
  (fn [{:keys [model key] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :model :key) elems)]
      (let [{:keys [data]} model
            update-form-machine #(swap! data assoc key (.. (in e) -value))]
        (doto (in e)
          (.addEventListener "keyup" update-form-machine)
          (.addEventListener "input" update-form-machine))
        (bind-in! e [in .-value] (cell= (get data key)))))))

(def line (-> h/input box destyle fieldable node parse-args))

(defn page []
  (let [default {:name "Default Joe"}
        {:keys [data]
         :as   fm} {:data (cell default)}]
    (window
      :title "Form defaults" :p (em 1)
      (form
        :model fm
        :g 8
        (line :model fm
              :key :name
              :sh (em 20) :b 1 :bc (c 0 0.1) :p 4)
        (elem "Reset"
              :c (c 0 0.3) :p 4
              :click #(reset! (:data fm) default))
        (elem :sh (r 1 1)
              (cell= (str "Form model: " data)))))))
