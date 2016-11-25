(ns hoplon.ui.plus)

(defmacro form [& args]
  `(hoplon.binding/binding
     [hoplon.ui/*data* (javelin.core/cell {})
      hoplon.ui/*error* (atom nil)
      hoplon.ui/*submit* (atom nil)]
     (form* ~@args)))
