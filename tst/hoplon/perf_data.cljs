(ns hoplon.perf_data)

(def gaw-logo "https://www.gawcapital.com/img/Tab_Logo.png")
(def sponsor-logo (str "url('" gaw-logo "') left/contain no-repeat"))

(def tower
  {:db/id                 :tower
   :deal/type             :deal.type/real-estate
   :deal/contract-addr    "0x48bc51c0aebba2e6d275b84554a088ca96207b12"
   :subtype               "Real Assets"
   :strategy              "Real Asset"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/total-amount     300e6
   :deal/sponsor-logo     sponsor-logo
   :deal/cover            "url('/towerhill.png') center/cover no-repeat"
   :cap-struct            "Pref. Equity"
   :deal/fees             "1.25%/16%"
   :deal/name             "Project Tower Hill"
   :subtitle              "43 Tower Hill Road, London EC1"
   :deal/sponsor          "Gaw Capital Partners"
   :lat                   51.5100501
   :lng                   -0.0749666
   :min-amount            20000
   :deal/duration         5
   :deal/usd-irr          0.231
   :deal/moic             1.7
   :deal/carried-interest 0.15
   :deal/currency         "GBP"})

(def forum-iv
  {:db/id                 :forum-iv
   :deal/type             :deal.type/fund
   :deal/name             "Forum Asian Realty Income IV"
   :deal/sponsor          "Forum Partners Asia Private Equity & Debt"
   :subtype               "Fund"
   :strategy              "Debt"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/total-amount     500e6
   :deal/usd-irr          0.148
   :deal/moic             2.0
   :deal/currency         :USD
   :min-amount            50000
   :deal/duration         5
   :deal/fees             "5.84%"
   :deal/carried-interest 0.12})

(def everbright-ashmore-ii
  {:db/id                 :everbright-ashmore-ii
   :deal/type             :deal.type/fund
   :deal/name             "Everbright Ashmore China Real Estate Fund II"
   :deal/sponsor          "Forum Partners Asia Private Equity & Debt"
   :subtype               "Fund"
   :strategy              "Value Add"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/usd-irr          0.207
   :deal/moic             1.4
   :deal/currency         :USD
   :min-amount            10000
   :deal/duration         3
   :deal/fees             "3.00%"
   :deal/carried-interest 0.15})

(def carlyle-asia-ii
  {:db/id                 :carlyle-asia-ii
   :deal/type             :deal.type/fund
   :deal/name             "Carlyle Asia Real Estate Fund II"
   :deal/sponsor          "Carlyle Asia Real Estate Partners"
   :subtype               "Fund"
   :strategy              "Debt"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/total-amount     750e6
   :deal/usd-irr          0.25
   :deal/moic             1.9
   :deal/currency         :USD
   :min-amount            100000
   :deal/duration         7
   :deal/fees             "4.62%"
   :deal/carried-interest 0.18})

(def sccapital-core
  {:db/id                 :sccapital-core
   :deal/type             :deal.type/fund
   :deal/name             "SC Core Fund"
   :deal/sponsor          "SC Capital Partners"
   :subtype               "Fund"
   :strategy              "Debt"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/usd-irr          0.18
   :deal/moic             1.5
   :deal/currency         :USD
   :min-amount            10000
   :deal/duration         5
   :deal/fees             "3.00%"
   :deal/carried-interest 0.2})

(def lasalle-opportunity-ii
  {:db/id                 :lasalle-opportunity-ii
   :deal/type             :deal.type/fund
   :deal/name             "LaSalle Asia Opportunity Fund II"
   :deal/sponsor          "LaSalle Asia Pacific"
   :subtype               "Fund"
   :strategy              "Opportunistic"
   :deal/region           "APAC"
   :deal/offering         "Secondary"
   :deal/sponsor-logo     sponsor-logo
   :deal/usd-irr          0.20
   :deal/moic             1.7
   :deal/currency         :USD
   :min-amount            10000
   :deal/duration         7
   :deal/fees             "3.12%"
   :deal/carried-interest 0.16})

(def pag-i
  {:db/id                 :pag-i
   :deal/type             :deal.type/fund
   :deal/name             "PAG Real Estate Partners"
   :deal/sponsor          "PAG Real Estate"
   :subtype               "Fund"
   :strategy              "Distressed"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/total-amount     1500e6
   :deal/usd-irr          0.228
   :deal/moic             2.0
   :deal/currency         :USD
   :min-amount            100000
   :deal/duration         5
   :deal/fees             "5.5%"
   :deal/carried-interest 0.2})

(def balmain-private-debt
  {:db/id                 :balmain-private-debt
   :deal/type             :deal.type/fund
   :deal/name             "Balmain Investment Management Secured Private Debt Fund"
   :deal/sponsor          "Balmain Investment Management"
   :subtype               "Fund"
   :strategy              "Debt"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/total-amount     384e6
   :deal/usd-irr          0.18
   :deal/moic             1.8
   :deal/currency         :USD
   :min-amount            25000
   :deal/duration         3
   :deal/fees             "6.18%"
   :deal/carried-interest 0.12})

(def century-bridge-china
  {:db/id                 :century-bridge-china
   :deal/type             :deal.type/fund
   :deal/name             "Century Bridge China Real Estate Fund "
   :deal/sponsor          "Century Bridge Capital"
   :subtype               "Fund"
   :strategy              "Value Add"
   :deal/region           "APAC"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/total-amount     400e6
   :deal/usd-irr          0.14
   :deal/moic             1.5
   :deal/currency         :USD
   :min-amount            10000
   :deal/duration         3
   :deal/fees             "2.5%"
   :deal/carried-interest 0.15})

(defn img [deal img-name]
  (str "//" "docs-bucket" ".s3.amazonaws.com/" (name deal) "/" img-name))

(def corestate-high-street-vi
  {:db/id                 :corestate-high-street-vi
   :deal/type             :deal.type/fund
   :deal/name             "Project High Street VI"
   :subtitle              "Investment in a High Street Retail Portfolio in medium-sized German cities"
   :deal/sponsor          "Corestate Capital"
   :subtype               "Fund"
   :strategy              "Core+"
   :deal/region           "GERMANY"
   :deal/offering         "Primary"
   :deal/sponsor-logo     sponsor-logo
   :deal/cover            (str "url('" (img :corestate-high-street-vi "cover.png") "') center/contain no-repeat")
   :deal/total-amount     94
   :deal/gross-usd-irr    0.134
   :deal/moic             1.59
   :deal/currency         "€ "
   :min-amount            100000
   :deal/duration         4
   :deal/fees             "TBC"
   :deal/carried-interest 0.20})

(def example-fund
  {:db/id                 :example-fund
   :deal/type             :deal.type/fund
   :deal/name             "Example Fund"
   :deal/sponsor          "Example Issuer"
   :subtype               "Fund"
   :strategy              "<STRATEGY>"
   :deal/region           "<REGION>"
   :deal/offering         "<OFFERING>"
   :deal/sponsor-logo     sponsor-logo
   :deal/cover            "url('/placeholder.jpg') center/cover no-repeat"
   :deal/total-amount     500e6
   :deal/usd-irr          0.148
   :deal/moic             2.0
   :deal/currency         :GBP
   :min-amount            50000
   :deal/duration         5
   :deal/fees             "5.84%"
   :deal/carried-interest 0.12})

(def deals-map
  {:tower                    tower
   :forum-iv                 forum-iv
   :everbright-ashmore-ii    everbright-ashmore-ii
   :carlyle-asia-ii          carlyle-asia-ii
   :sccapital-core           sccapital-core
   :lasalle-opportunity-ii   lasalle-opportunity-ii
   :pag-i                    pag-i
   :balmain-private-debt     balmain-private-debt
   :century-bridge-china     century-bridge-china
   :corestate-high-street-vi corestate-high-street-vi
   :example-fund             example-fund})

(def deals (-> deals-map vals vec))

(defn type->name [deal-type]
  (-> {:deal.type/fund        "Fund"
       :deal.type/real-estate "Real Estate"}
      (get deal-type "<Type>")))

(def currency-short-forms
  {"USD" "$"
   "HKD" "HK$"
   "GBP" "£"
   "EUR" "€"})

(defn currency-str
  "Accepts an all-caps 3 letter currency keyword or string
  coerces it to a string. `nil` means USD.

  eg:
  :ANY-KW -> \"ANY-KW\"
  \"ANY-STR\" -> \"ANY-STR\""
  [currency]
  (as-> (or currency "USD") c
        (if (keyword? c) (name c) c)))

(defn curr-str
  "Accepts an all-caps 3 letter currency keyword or string
  coerces it to a string and may return a short form of it if available.

  eg:
  :ANY-KW -> \"ANY-KW\"
  \"ANY-STR\" -> \"ANY-STR\"
  :USD -> \"$\"
  \"HKD\" -> \"HK$\""
  [currency]
  (as-> (currency-str currency) c
        (get currency-short-forms c c)))

(defn fmt-num [& [num]]
  (if num (.toLocaleString num) "N/A"))

(defn fmt-curr [& [num currency]]
  (str (currency-str (or currency :USD)) " " (fmt-num num)))

(defn percent-fmt
  "Transform a decimal to percentage"
  [value]
  (if value
    (str (.toFixed (clojure.core/* value 100) 1) "%")
    "N/A"))

(defn moic-fmt [value]
  (if value (str (.toFixed value 1) "x") "N/A"))

(defn period-fmt [value]
  (when value (str value " Years")))

(defn as-str [deal]
  (-> deal
      (update :type-name #(type->name (:deal/type deal)))
      (update :deal/usd-irr percent-fmt)
      (update :deal/gross-usd-irr percent-fmt)
      (update :avg-ret percent-fmt)
      (update :benchmark percent-fmt)
      (update :deal/moic moic-fmt)
      (update :deal/duration period-fmt)
      (update :min-amount fmt-curr (:deal/currency deal))
      (update :deal/carried-interest percent-fmt)
      (update :deal/total-amount fmt-curr (:deal/currency deal))))
