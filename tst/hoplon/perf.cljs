(ns hoplon.perf
  (:require
    [javelin.core :as j :refer [defc defc= cell= cell cell-let with-let]]
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [hoplon.ui :as hui :refer [window elem bind-in!]]
    [hoplon.ui.attrs :refer [c r d em px]]
    [hoplon.ui.elems :refer [in]]

    [material.ui :as mui :refer [container row paper]]
    [material.colors :as c]

    [hoplon.perf_data :refer [deals as-str]]))

(defn primary-metric [label text]
  (row :gv 4
       (row :ah :beg :fc c/secondary-text label)
       (row :ah :beg :f (em 1.8) :fc c/primary-text text)))

(defn secondary-metric [label text]
  (row :f mui/f-md :pb mui/pad-sm :av :mid
       (elem :sh (r 2 3) :ah :beg :fc c/dark-primary-color label)
       (elem :sh (r 1 3) :ah :end text)))

(defn image-section [url]
  (str "url('" url "')  left/contain no-repeat"))

(def image-placeholder
  "/placeholder.jpg")

(defn card [deal]
  (cell-let
    [{id               :db/id
      type             :deal/type
      type-name        :type-name
      title            :deal/name
      duration         :deal/duration
      fees             :deal/fees
      usd-irr          :deal/usd-irr
      carried-interest :deal/carried-interest
      moic             :deal/moic
      logo             :deal/sponsor-logo
      strategy         :deal/strategy
      region           :deal/region
      offering         :deal/offering
      total-amount     :deal/total-amount
      sponsor          :deal/sponsor
      deal-of          :deal/of} (cell= (as-str deal))
     ; FIXME border-color throws this error:
     ; Uncaught Error:
     ;   No protocol method IAttr.-dom-attribute defined
     ;     for type javelin.core/Cell: [object Object]
     border-color (cell= (if type
                           (if (= type :deal.type/fund)
                             c/primary-color
                             c/accent-color)
                           c/primary-color))]
    (mui/paper
      :sh 330 :c c/white
      :bv 4
      :bc c/accent-color
      :p 0
      (container :p mui/pad-md :gv mui/g-md
        (row :sv 80
             (mui/section
               :sv 50 :sh 130
               :background (cell= (or logo
                                      (image-section image-placeholder))))
             (elem :sh (- (r 1 1) 130)
                   :fc c/dark-primary-color
                   :f mui/f-sm
                   :ah :end
                   (cell= (str (or strategy type-name) " | "
                               (or region "<REGIONXXX>") " | "
                               (or offering "Primary")))))
        (row :f mui/f-lg :sv 80
             (cell= (or title "<Deal Name>"))
             (row :ft :300 :f mui/f-md
                  (cell= (or sponsor
                             (:org/name deal-of)
                             "<Sponsor Name>")))
             (row total-amount))
        (elem
          (primary-metric "Expected Net Investor IRR" (cell= (or usd-irr "TBC")))
          (row "")
          (primary-metric "Expected Net Investor MOIC" (cell= (or moic "TBC")))
          (row :pv mui/pad-sm :ah :mid "+ + + +")
          ;(secondary-metric "Minimum Investment" min-amount)
          (secondary-metric "Investment Period" duration)
          (secondary-metric "Total Management Fees" fees)
          (secondary-metric "Carried Interest" carried-interest)
          (row :sv mui/pad-lg)
          (mui/raised-button
            "Learn More"
            :click identity
            :sh (r 1 1) mui/primary :f mui/f-md :a :mid))))))

(defn page []
  (window
    (container :ah :mid :g 14
      (for-tpl [deal deals]
        (card deal)))))
