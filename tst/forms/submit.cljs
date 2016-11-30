(ns forms.submit
  (:require
    [cljs.pprint :as pp]
    [javelin.core :as j :refer [defc defc= cell= cell cell-let with-let]]
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [hoplon.ui :as hui :refer [window elem bind-in!]]
    [hoplon.ui.plus :as hui+]
    [hoplon.ui.attrs :refer [c r d em px]]
    [hoplon.ui.elems :refer [in]]))

(defn page []
  (window
    ))