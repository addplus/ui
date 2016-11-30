(ns ^{:hoplon/page "index.html"} hoplon.index
  (:require
    [hoplon.ui-test]
    [hoplon.forms]
    [forms.chains]
    ;[forms.conditional]
    ;[forms.defaults]
    ;[forms.submit]
    ;[forms.types]
    ;[forms.updates]
    ;[forms.validation]
    ))

(defn page []
  ;(hoplon.demo/page)
  ;(hoplon.ui-test/page)
  (forms.chains/page)
  ;(forms.conditional/page)
  ;(forms.defaults/page)
  ;(forms.submit/page)
  ;(forms.types/page)
  ;(forms.updates/page)
  ;(forms.validation/page)
  ;(hoplon.forms/page)
  )

(page)
