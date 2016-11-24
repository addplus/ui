(ns material.ui)

(defmacro form-with
  "Create form with initial data provided"
  [{:keys [data]} & args]
  `(let [initial-form-data# ~data
         changed-form-data# (javelin.core/cell nil)]
     (binding [hoplon.ui/*data* (javelin.core/cell=
                                  (merge initial-form-data# changed-form-data#)
                                  #(reset! changed-form-data# %))
               hoplon.ui/*error* (atom nil)
               hoplon.ui/*submit* (atom nil)]
       (hoplon.ui/form* ~@args))))
