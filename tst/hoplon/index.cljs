(ns ^{:hoplon/page "index.html"} hoplon.index
  (:require
    [hoplon.demo]
    [hoplon.ui-test]
    [hoplon.forms]))

(defn page []
  ;(hoplon.demo/page)
  ;(hoplon.ui-test/page)
  (hoplon.forms/page))

(page)
