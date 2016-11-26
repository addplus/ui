(ns hoplon.ui
  (:refer-clojure :exclude [binding bound-fn])
  (:require
    [hoplon.core          :as h]
    [hoplon.ui.validation :as v]
    [clojure.string  :refer [blank? join split ends-with?]]
    [cljs.reader     :refer [read-string]]
    [javelin.core    :refer [cell cell?]]
    [hoplon.ui.attrs :refer [r em ratio? calc? ->attr]]
    [hoplon.ui.elems :refer [box doc out mid in elem?]]
    [hoplon.binding]
    [cljsjs.markdown])
  (:require-macros
    [hoplon.core    :refer [with-timeout]]
    [hoplon.binding :refer [binding bound-fn]]
    [hoplon.ui      :refer [set-in! bind-in!]]
    [javelin.core   :refer [cell= with-let set-cell!=]]))

;;; constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *position*   nil)
(def ^:dynamic *clicks*     nil)
(def ^:dynamic *pointer*    nil)
(def ^:dynamic *state*      nil)

(def empty-icon-url  "data:;base64,iVBORw0KGgo=")
(def empty-image-url "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==")

;;; utils ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn route->hash [[path & [qmap]]]
  "transforms a urlstate of the form [[\"foo\" \"bar\"] {:baz \"barf\"}]
   to hash string in the form \"foo/bar&baz=barf\""
  (let [pair (fn [[k v]] (str (name k) "=" (js/encodeURI (pr-str v))))
        pstr (when (not-empty path) (apply str "/" (interpose "/" (map name path))))
        qstr (when (not-empty qmap) (apply str "?" (interpose "&" (map pair qmap))))]
    (str pstr qstr)))

(defn hash->route [hash]
  "transforms a hash string to a urlstate of the form
   [[\"foo\" \"bar\"] {:baz \"barf\"}]"
  (let [[rstr qstr] (split (subs hash 1) #"\?")
        pair        #(let [[k v] (split % #"=" 2)]
                          [(keyword k) (read-string (js/decodeURI v))])
        qmap        (->> (split qstr #"&") (map pair) (when (not-empty qstr)) (into {}))
        path        (->> (split rstr #"/") (remove empty?) (mapv keyword))]
    (vec [path qmap])))

(defn clean [map] (into {} (filter second map)))

(defn copy! [text]
  (let [foc (.-activeElement js/document)
        tgt (with-let [e (.createElement js/document "textarea")]
              (set! (.-value e) text))]
    (.appendChild (.-body js/document) tgt)
    (.focus tgt)
    (.setSelectionRange tgt 0 (.. tgt -value -length))
    (.execCommand js/document "copy")
    (.focus foc)
    (.removeChild (.-body js/document) tgt)))

(defn debounce [ms f]
  (let [id (atom nil)]
    (fn [& args]
      (js/clearTimeout @id)
      (reset! id (js/setTimeout #(apply f args) ms)))))

(def visibility->status
  "maps the visibility string to a status keyword"
  {"visible"   :foreground
   "hidden"    :background
   "prerender" :background
   "unloaded"  :terminated})

(defn bind-with! [f value]
  (if (cell? value) (f @(add-watch value (gensym) #(f %4))) (f value)))

(defn vstr [vs]
  (join " " (mapv ->attr vs)))

(defn swap-elems! [e f v] ;; todo: factor out
  (cond (cell?       e) (cell= (swap-elems! e f v))
        (sequential? e) (doseq [e e] (swap-elems! e f v)) ;;todo: handled with IElemValue if (hoplon.ui/elem?)
        (elem?       e) (f e v)))

;;; attribute middlewares ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn size [ctor]
  "set the size on the outer element when it is expressed as a ratio, and on the
   inner element when it is a length.

   since ratios are expressed in terms of the parent, they include the margin
   (implemented as the padding between the inner and outer elements). fixed
   lengths are set on the middle, however, to exclude the margin so that it will
   push out against the parent container instead of being subtracted from the
   size of the elem.  both the inner and middle elements are bound separately to
   accomodate cells that might return ratios, evals, and fixed sizes at
   different times, such as the cell returned by the breakpoints function.

   when the collective size of the elem's children is greater than an explicitly
   set size in the vertical orientation, a scrollbar will automatically appear.
   horizontal scrolling is disallowed due to the fact that the browser does not
   permit them to be set to auto and visible independently; setting oveflowX to
   auto in the horizontal will set it to auto in the vertical as well, even if
   it is explictly set to visible."
  (fn [{:keys [s sh sv sh- sh+ scroll] :as attrs} elems]
    {:pre [(v/lengths? s sh sv sh- sh+)]}
    (with-let [e (ctor (dissoc attrs :s :sh :sv :sh- :sh+ :scroll) elems)]
      (let [rel? #(or (ratio? %) (calc? %))
            rel  #(cell= (if (rel? %) % %2))
            fix  #(cell= (if (rel? %) %2 %))]
        (bind-in! e [out .-style .-width]     (rel (or sh s) nil))
        (bind-in! e [out .-style .-minWidth]  (rel sh- nil))
        (bind-in! e [out .-style .-maxWidth]  (rel sh+ nil))
        (bind-in! e [out .-style .-height]    (rel (or sv s) nil))
        (bind-in! e [mid .-style .-width]     (fix (or sh s) nil))
        (bind-in! e [mid .-style .-minWidth]  (fix sh- nil))
        (bind-in! e [mid .-style .-maxWidth]  (fix sh+ nil))
        (bind-in! e [mid .-style .-height]    (fix (or sv s) nil))
        (bind-in! e [mid .-style .-maxHeight] (fix (or sv s) nil))
        (bind-in! e [in  .-style .-overflowY] (cell= (when (and scroll (or sv s)) :auto))))))) ;; default likely breaks 100% height where a sibling overflows

(defn align [ctor]
  "set the text-align and vertical-align attributes on the elem and proxy the
   vertical-align attribute to the outer element of each child.  set vertical
   height of the inner element to auto when the align vertical attribute is set.

  the vertical alignment is proxied to the outer elements of the children so
  that, in addition to aligning the lines of children within the elem, the
  children are also aligned in the same manner within their respective lines."
  (fn [{:keys [a ah av] :as attrs} elems]
    {:pre [(v/aligns? a) (v/alignhs? ah) (v/alignvs? av)]}
    (let [pv (cell= ({:beg "0%"  :mid "50%"   :end "100%"}               (or av a) "0%"))
          ah (cell= ({:beg :left :mid :center :end :right :jst :justify} (or ah a) (or ah a)))
          av (cell= ({:beg :top  :mid :middle :end :bottom}              (or av a) (or av a)))]
      (swap-elems! elems #(bind-in! %1 [out .-style .-verticalAlign] %2) (cell= (or av :top)))
      (with-let [e (ctor (dissoc attrs :a :ah :av) elems)]
        (bind-in! e [in  .-style .-height]        (cell= (if av :auto "100%"))) ;; height is 100% only when based on size of children
        (bind-in! e [mid .-style .-textAlign]     ah)
        (bind-in! e [mid .-style .-verticalAlign] av)
        (when (= (-> e in .-style .-position) "absolute")
          (bind-in! e [in .-style .-top]       pv)
          (bind-in! e [in .-style .-transform] (cell= (str "translateY(-" pv ")"))))))))

(defn pad [ctor]
  "set the padding on the elem's inner element.

   this adds space between the edges of the container and its children."
  (fn [{:keys [p ph pv pl pr pt pb] :as attrs} elems]
    {:pre [(v/lengths? p ph pv pl pr pt pb)]}
    ;; todo: dissallow pct based paddings since tied to opposite dimension
    (with-let [e (ctor (dissoc attrs :p :ph :pv :pl :pr :pt :pb) elems)]
      (bind-in! e [mid .-style .-paddingLeft]   (or pl ph p))
      (bind-in! e [mid .-style .-paddingRight]  (or pr ph p))
      (bind-in! e [mid .-style .-paddingTop]    (or pt pv p))
      (bind-in! e [mid .-style .-paddingBottom] (or pt pv p)))))

(defn gutter [ctor]
  "set the padding on the outer element of each child and a negative margin on
   the inner element of the elem itself equal to the padding.

   outer padding on the children creates an even gutter between them, while the
   negative inner margin on the elem itself offsets this padding to fencepost
   the children flush with the edges of the container."
  (fn [{:keys [g gh gv] :as attrs} elems]
    {:pre [(v/lengths? g gh gv)]}
    (let [mh (cell= (/ (or gh g) 2))
          mv (cell= (/ (or gv g) 2))
          ph (cell= (- mh))
          pv (cell= (- mv))]
      (swap-elems! elems #(do (bind-in! % [out .-style .-paddingLeft]   %2)
                              (bind-in! % [out .-style .-paddingRight]  %2)) mh)
      (swap-elems! elems #(do (bind-in! % [out .-style .-paddingTop]    %2)
                              (bind-in! % [out .-style .-paddingBottom] %2)) mv)
      (with-let [e (ctor (dissoc attrs :g :gh :gv) elems)]
        (bind-in! e [in .-style .-marginLeft]   ph)
        (bind-in! e [in .-style .-marginRight]  ph)
        (bind-in! e [in .-style .-marginTop]    pv)
        (bind-in! e [in .-style .-marginBottom] pv)))))

(defn color [ctor]
  "set the background color an the inner element."
  (fn [{:keys [c o m v l] :as attrs} elems]
    {:pre [(v/colors? c) (v/opacities? o) (v/cursors? m)]}
    ;; todo: linking user select to cursor
    (with-let [e (ctor (dissoc attrs :c :o :m :v :l) elems)]
      (let [l (cell= (if l :text :none))]
        (bind-in! e [mid .-style .-backgroundColor]  c)
        (bind-in! e [mid .-style .-opacity]          o)
        (bind-in! e [mid .-style .-cursor]           m)
        (bind-in! e [out .-style .-visibility]       (cell= (when (and (contains? attrs :v) (not v)) :hidden)))
        (bind-in! e [in  .-style .-userSelect]       l)
        (bind-in! e [in  .-style .-mozUserSelect]    l)
        (bind-in! e [in  .-style .-msUserSelect]     l)
        (bind-in! e [in  .-style .-webkitUserSelect] l)))))

(defn transform [ctor]
  "apply a taransformation on the outer element."
  (fn [{:keys [x xx xy xz xb xs] :as attrs} elems]
    {:pre [(v/transforms? x) (v/origins? xx xy xz) (v/boxes? xb) (v/txstyles? xs)]}
    (with-let [e (ctor (dissoc attrs :x :xx :xy :xz :xb :xs) elems)]
      (bind-in! e [out .-style .-transform]       x)
      (bind-in! e [out .-style .-transformOrigin] (cell= (vstr (vector xx xy xz)))) ;; todo: remove vstr
      (bind-in! e [out .-style .-transformBox]    xb)
      (bind-in! e [out .-style .-transformStyle]  xs))))

(defn round [ctor]
  "set the radius on the middle element."
  (fn [{:keys [r rtl rtr rbl rbr] :as attrs} elems]
    {:pre [(v/lengths? r rtl rtr rbl rbr)]}
    (with-let [e (ctor (dissoc attrs :r :rtl :rtr :rbl :rbr) elems)]
      (bind-in! e [mid .-style .-borderTopLeftRadius]     (or rtl r))
      (bind-in! e [mid .-style .-borderTopRightRadius]    (or rtr r))
      (bind-in! e [mid .-style .-borderBottomLeftRadius]  (or rbl r))
      (bind-in! e [mid .-style .-borderBottomRightRadius] (or rbr r)))))

(defn shadow [ctor]
  "set the shadows on the middle element."
  (fn [{:keys [d] :as attrs} elems]
    {:pre [(v/shadows? d)]}
    (with-let [e (ctor (dissoc attrs :d) elems)]
      (bind-in! e [mid .-style .-boxShadow] d))))

(defn border [ctor]
  "set the border on the elem's middle element.

   this adds space between the edges of the container and its children."
  (fn [{:keys [b bh bv bl br bt bb bc bch bcv bcl bcr bct bcb] :as attrs} elems]
    {:pre [(v/lengths? b bh bv bl br bt bb) (v/colors? bc bch bcv bcl bcr bct bcb)]}
    (with-let [e (ctor (dissoc attrs :b :bh :bv :bl :br :bt :bb :bw :bc :bch :bcv :bcl :bcr :bct :bcb) elems)]
      (bind-in! e [mid .-style .-borderLeftWidth]   (or bl bh b))
      (bind-in! e [mid .-style .-borderRightWidth]  (or br bh b))
      (bind-in! e [mid .-style .-borderTopWidth]    (or bt bv b))
      (bind-in! e [mid .-style .-borderBottomWidth] (or bb bv b))
      (bind-in! e [mid .-style .-borderLeftColor]   (or bcl bch bc "transparent"))
      (bind-in! e [mid .-style .-borderRightColor]  (or bcr bch bc "transparent"))
      (bind-in! e [mid .-style .-borderTopColor]    (or bct bcv bc "transparent"))
      (bind-in! e [mid .-style .-borderBottomColor] (or bcb bcv bc "transparent"))
      (set-in!  e [mid .-style .-borderStyle]       "solid"))))

(defn fontable [ctor]
    "- f  font size
     - ft font weight
     - fw letter spacing
     - fh line height
     - ff font family
     - fc font color
     - fu text decoration
     - fi font style
     - fk font kerning
     - fr text rendering
     - fa font size adjust
     - fm font smoothing
     - fx font transform
     - fz font stretch
     - fy font synthesis
     - fx font capitalize"
  (fn [{:keys [f fw fh ft ff fc fu fi fk fa fs fx fy fr fm] :as attrs} elems]
    {:pre [(v/sizes? f) (v/spacings? fw fh) (v/weights? ft) (v/families? ff) (v/colors? fc) (v/decorations? fu) (v/styles? fi) (v/adjusts? fa) (v/stretches? fs) (v/syntheses? fy) (v/renderings? fr) (v/smoothings? fm) (v/capitalizes? fx)]}
    (with-let [e (ctor (dissoc attrs :f :fw :fh :ft :ff :fc :fu :fi :fk :fa :fs :fx :fy :fr :fm) elems)]
      (bind-in! e [in .-style .-fontSize]               f)
      (bind-in! e [in .-style .-letterSpacing]          fw)
      (bind-in! e [in .-style .-lineHeight]             fh)
      (bind-in! e [in .-style .-fontWeight]             ft)
      (bind-in! e [in .-style .-fontFamily]             ff)
      (bind-in! e [in .-style .-color]                  fc)
      (bind-in! e [in .-style .-textDecoration]         fu)
      (bind-in! e [in .-style .-fontStyle]              fi)
      (bind-in! e [in .-style .-fontKerning]            fk)
      (bind-in! e [in .-style .-textRendering]          fr)
      (bind-in! e [in .-style .-fontSizeAdjust]         fa)
      (bind-in! e [in .-style .-webkitFontSmoothing]    fm)
      (bind-in! e [in .-style .-moz-osx-font-smoothing] (case fm :antialiased :greyscale :none :unset :initial))
      (bind-in! e [in .-style .-fontSmooth]             (case fm :antialiased :always    :none :never :initial))
      (bind-in! e [in .-style .-fontStretch]            fs)
      (bind-in! e [in .-style .-textTransform]          fx)
      (bind-in! e [in .-style .-fontSynthesis]          fy))))

(defn destyle [ctor]
  "neutralize the default styling of the inner element.

  this allows native components to be styled freely using attributes mapped to
  the middle element."
  (fn [attrs elems]
    (with-let [e (ctor attrs elems)]
      (bind-in! e [in .-style .-width]           (r 1 1)) ;; display block should force to 100%
      (bind-in! e [in .-style .-height]          (r 1 1))
      (bind-in! e [in .-style .-outline]         :none)
      (bind-in! e [in .-style .-backgroundColor] :transparent)
      (bind-in! e [in .-style .-borderStyle]     :none)
      (bind-in! e [in .-style .-textAlign]       :inherit)))) ;; cursor: pointer, :width: 100%

;;; form middlewares ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *data*   nil)
(def ^:dynamic *error*  nil)
(def ^:dynamic *submit* nil)

(defn formable [ctor]
  "set up a form context"
  (fn [{:keys [change submit] :as attrs} elems]
    (when change (cell= (change (clean *data*))))           ;; init *data* to value of form fields on render
    (with-let [e (ctor (dissoc attrs :change :submit) elems)]
      (.addEventListener (in e) "keypress" (bound-fn [e] (when (= (.-which e) 13) (submit (clean @*data*))))))))

(defn fieldable [ctor]
  "set the values common to all form fields."
  (fn [{:keys [key val req autofocus] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :key :val :req :autofocus :debounce) elems)]
      (let [save (bound-fn  [_]  (when *data*  (swap! *data* assoc  (read-string  (.-name  (in e)))  (not-empty  (.-value  (in e))))))]
        (.addEventListener (in e) "change" save)
        (.addEventListener (in e) "keyup"  (if-let [deb (:debounce attrs)] (debounce deb save) save))
        (bind-in! e [in .-name]     (cell= (pr-str key)))
        (bind-in! e [in .-value]    val)
        (bind-in! e [in .-required] (cell= (when req :required)))
        (bind-in! e [in .-autofocus] autofocus)))))

(defn file-field [ctor]
  (fn [{:keys [accept] :as attrs} elems]
    ;{:pre []} ;accept [".jpg" ".png" "audio/*" "video/*" "image/*" "application/ogg"]
    (with-let [e (ctor (dissoc attrs :accept) elems)]
      (let [i (.appendChild (mid e) (.createElement js/document "input"))]
        (bind-in! i [.-style .-position] "absolute")
        (bind-in! i [.-style .-left]     "0")
        (bind-in! i [.-style .-width]    "100%")
        (bind-in! i [.-style .-top]      "0")
        (bind-in! i [.-style .-bottom]   "0")
        (bind-in! i [.-style .-opacity]  "0")
        (bind-in! i [.-type]             "file")
        (bind-in! (mid e) [.-tabIndex]   "0")
        (.addEventListener i "change" (bound-fn [_] (when *data* (swap! *data* assoc (read-string (.-name i)) (when (not-empty (.-value i)) {:name (.-value i) :data (.-name (.item (.-files i) 0))})))
                                                    (when-let [v (not-empty (.-value i))] (set! (.-innerHTML (in e)) (last (split v #"\\"))))))))));"

(defn pick-field [ctor]
  (fn [{:keys [selection] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :selection) elems)]
      #_(bind-in! e [in .-name] key))))

(defn picks-field [ctor]
  (fn [{:keys [selections] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :selections) elems)]
      #_(bind-in! e [in .-name] key))))

(defn item-field [ctor]
  (fn [{:keys [val] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :val) elems)]
      (bind-in! e [in .-value] (cell= (pr-str val)))
      (.addEventListener (mid e) "mousedown" (bound-fn [] (when (= *state* :on) nil #_(reset! *selected* val)))))))

(defn items-field [ctor]
  (fn [{:keys [val] :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :val) elems)]
      (bind-in! e [in .-value] (cell= (pr-str val)))
      (.addEventListener (mid e) "mousedown" (bound-fn [] (if (= *state* :on) nil #_(reset! *selected* val)))))))

(defn line-field [ctor]
  (fn [{:keys [rows cols autocomplete autocapitalize content prompt charsize charmin charmax resizable] :as attrs} elems]
    {:pre [(v/autocompletes? autocomplete) (v/autocapitalizes? autocapitalize) (v/contents? content) (v/integers? charsize charmin charmax)]}
    (with-let [e (ctor (dissoc attrs :rows :cols :autocomplete :autocapitalize :content :prompt :charsize :charmin :charmax :resizeable) elems)]
      (bind-in! e [in .-style .-padding] "0")
      (bind-in! e [in .-rows]            (cell= (if rows (str rows) "1")))
      (bind-in! e [in .-style .-height]  (cell= (if rows nil "100%")))
      (bind-in! e [in .-cols]            (cell= (when cols (str cols))))
      (bind-in! e [in .-style .-width]   (cell= (if cols nil "100%")))
      (bind-in! e [in .-style .-resize]  (cell= (or resizable :none)))
      (bind-in! e [in .-type]            content)
      (bind-in! e [in .-placeholder]     prompt)
      (bind-in! e [in .-autocomplete]    autocomplete)
      (bind-in! e [in .-autocapitalize]  autocapitalize)

      ;(bind-in! e [in .-size]           charsize)
      (bind-in! e [in .-minlength]       charmin)
      (bind-in! e [in .-maxlength]       charmax))))

(defn lines-field [ctor]
  (fn [{:keys [rows cols autocomplete autocapitalize content prompt charsize charmin charmax resizable] :as attrs} elems]
    {:pre [(v/autocompletes? autocomplete) (v/autocapitalizes? autocapitalize) (v/contents? content) (v/integers? charsize charmin charmax)]}
    (with-let [e (ctor (dissoc attrs :rows :cols :autocomplete :autocapitalize :content :prompt :charsize :charmin :charmax :resizeable) elems)]
      (bind-in! e [in .-style .-padding] "0")
      (bind-in! e [in .-rows]            (cell= (if rows (str rows) "2")))
      (bind-in! e [in .-style .-height]  (cell= (if rows nil "100%")))
      (bind-in! e [in .-cols]            (cell= (when cols (str cols))))
      (bind-in! e [in .-style .-width]   (cell= (if cols nil "100%")))
      (bind-in! e [in .-style .-resize]  (cell= (or resizable :none)))
      (bind-in! e [in .-type]            content)
      (bind-in! e [in .-placeholder]     prompt)
      (bind-in! e [in .-autocomplete]    autocomplete)
      (bind-in! e [in .-autocapitalize]  autocapitalize)

      ;(bind-in! e [in .-size]           charsize)
      (bind-in! e [in .-minlength]       charmin)
      (bind-in! e [in .-maxlength]       charmax))))

(defn send-field [ctor]
  (fn [{label :label submit' :submit :as attrs} elems]
    {:pre []} ;; todo: validate
    (with-let [e (ctor (dissoc attrs :label :submit) elems)]
      (.addEventListener (mid e) "click" (bound-fn [_] (or submit' *submit*) *data*))
      (bind-in! e [in .-type]  "button")
      (bind-in! e [in .-value] label))))

;;; middlewares ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn underlay [ctor element-ctor]
  (fn [{:keys [fit] :as attrs} elems]
    {:pre [(v/fits? fit)]}
    (with-let [e (ctor (dissoc attrs :fit) elems)]
      (let [u (.insertBefore (mid e) (element-ctor) (in e))
            f (some #{fit} #{:cover :contain})]
        (bind-in! u [.-style .-display]      :block)
        (bind-in! u [.-style .-position]     (cell= (if f :absolute :relative)))
        (bind-in! u [.-style .-left]         (cell= (when f                 "50%")))
        (bind-in! u [.-style .-top]          (cell= (when f                 "50%")))
        (bind-in! u [.-style .-width]        (cell= (when (not= fit :cover) "100%")))
        (bind-in! u [.-style .-height]       (cell= (when (= fit :fill)     "100%")))
        (bind-in! u [.-style .-minWidth]     (cell= (when (= fit :cover)    "100%")))
        (bind-in! u [.-style .-minHeight]    (cell= (when (= fit :cover)    "100%")))
        (bind-in! u [.-style .-transform]    (cell= (when f                 "translate(-50%,-50%)")))
        (bind-in! e [mid .-style .-overflow] (cell= (when (= fit :cover) :hidden)))
        (bind-in! e [in  .-style .-position] :absolute)
        (bind-in! e [in  .-style .-top]      0)
        (bind-in! e [in  .-style .-width]    "100%")))))

(defn frameable [ctor]
  (fn [{:keys [allow-fullscreen sandbox type url] :as attrs} elems]
    {:pre []} ;; todo
    (with-let [e (ctor (dissoc attrs :allow-fullscreen :sandbox :type :url) elems)]
      (bind-in! e [mid .-firstChild .-allowFullscreen] allow-fullscreen)
      (bind-in! e [mid .-firstChild .-sandbox]         sandbox)
      (bind-in! e [mid .-firstChild .-type]            type)
      (bind-in! e [mid .-firstChild .-src]             url))))

(defn imageable [ctor]
  (fn [{:keys [url] :as attrs} elems]
    {:pre []} ;; todo
    (with-let [e (ctor (dissoc attrs :url) elems)]
      (bind-in! e [mid .-firstChild .-src] url))))

(defn objectable [ctor]
  (fn [{:keys [cross-origin type url] :as attrs} elems]
    {:pre []} ;; todo
    (with-let [e (ctor (dissoc attrs :cross-origin :type :url) elems)]
      (bind-in! e [mid .-firstChild .-crossOrigin] cross-origin)
      (bind-in! e [mid .-firstChild .-type]        type)
      (bind-in! e [mid .-firstChild .-data]        url))))

(defn videoable [ctor]
  (fn [{:keys [autoplay controls loop muted poster url] :as attrs} elems]
    {:pre []} ;; todo
    (with-let [e (ctor (dissoc attrs :autoplay :controls :loop :muted :poster :url) elems)]
      (bind-in! e [mid .-firstChild .-autoplay] autoplay)
      (bind-in! e [mid .-firstChild .-controls] (cell= (when controls "controls")))
      (bind-in! e [mid .-firstChild .-loop]     loop)
      (bind-in! e [mid .-firstChild .-muted]    muted)
      (bind-in! e [mid .-firstChild .-poster]   poster)
      (bind-in! e [mid .-firstChild .-src]      url))))

(defn clickable [ctor]
  (fn [{:keys [click] :as attrs} elems]
    {:pre [(v/callbacks? click)]}
    (with-let [e (ctor (dissoc attrs :click) elems)]
      (when click
        (.addEventListener (mid e) "click" click)))))

(defn parse-args [ctor]
  (fn [& args]
     (apply ctor (#'hoplon.core/parse-args args))))

(defn interactive [ctor]
  (fn [attrs elems]
    (with-let [e (ctor attrs elems)]
      (.addEventListener (mid e) "mouseover" (bound-fn [] (swap! *pointer* update :over inc)))
      (.addEventListener (mid e) "mousedown" (bound-fn [] (swap! *pointer* update :down inc)))
      (.addEventListener (mid e) "mouseup"   (bound-fn [] (swap! *pointer* update :up   inc)))
      (.addEventListener (mid e) "mouseout"  (bound-fn [] (swap! *pointer* assoc  :out (inc (:out @*pointer*)) :up (:down @*pointer*)))))))

(defn selectable [ctor]
  (fn [attrs elems]
    (with-let [e (ctor attrs elems)]
      (let [switch #(if (odd? (:down %)) :on :off)]
        (set-cell!= *state* (switch *pointer*))))))

(defn toggleable [ctor]
  (fn [attrs elems]
    (with-let [e (ctor attrs elems)]
      (let [switch  #(if (odd? (:down %)) "on" "off")
            mouse   #(cond (not= (:over %) (:out %)) "over"
                           (not= (:down %) (:up  %)) "down"
                           :else                     "out")]
        (set-cell!= *state* (keyword (str (mouse *pointer*) "-" (switch *pointer*))))))))

(defn windowable [ctor]
  ;; todo: finish mousechanged
  (fn [{:keys [fonts icon language metadata route position scripts styles title initiated mousechanged positionchanged statuschanged routechanged scroll] :as attrs} elems]
    (let [get-hash   #(if (= (get js/location.hash 0) "#") (subs js/location.hash 1) js/location.hash)
          set-hash!  #(if (blank? %) (.replaceState js/history #js{} js/document.title ".") (set! js/location.hash %))
          get-route  (comp hash->route get-hash)
          set-route! (comp set-hash! route->hash)
          get-agent  #(-> js/window .-navigator)
          get-refer  #(-> js/window .-document .-referrer)
          get-status #(-> js/window .-document .-visibilityState visibility->status)]
        (with-let [e (ctor (dissoc attrs :fonts :icon :language :metadata :position :route :scripts :styles :title :initiated :mousechanged :positionchanged :statuschanged :routechanged :scroll) elems)]
          (bind-in! e [out .-lang] (or language "en"))
          (bind-in! e [out .-style .-width]     "100%")
          (bind-in! e [out .-style .-height]    "100%")
          (bind-in! e [mid .-style .-width]     "100%")
          (bind-in! e [mid .-style .-margin]    "0")
          (bind-in! e [mid .-style .-fontSize]  "100%")
          #_(bind-in! e [out .-style .-overflow] (cell= (when-not scroll "hidden")))
          (when initiated
            (initiated (get-route) (get-status) (get-agent) (get-refer)))
          (when routechanged
            (.addEventListener js/window "hashchange"
              #(when-not (= (route->hash @route) (get-hash)) (routechanged (get-route)))))
          (when statuschanged
            (.addEventListener js/window "visibilitychange"
              #(statuschanged (get-status))))
          (.addEventListener js/window "scroll"
            (bound-fn []
              (let [[x y :as new-position] (vector (.-scrollX js/window) (.-scrollY js/window))]
                (reset! *position* new-position)
                (when positionchanged
                  (when-not (= new-position *position*)
                    (positionchanged x y))))))
          (cell= (set-route! route))
          (.addEventListener js/document "DOMContentLoaded"
            #(cell= (.scroll js/window (first position) (second position))))
          (h/head
            (h/html-meta :charset "utf-8")
            (h/html-meta :http-equiv "X-UA-Compatible" :content "IE=edge")
            (h/html-meta :name "viewport"    :content "width=device-width, initial-scale=1")
            (for [m (if (map? metadata) (mapv (fn [[k v]] {:name k :content v}) metadata) metadata)]
              (h/html-meta (into {} (for [[k v] m] [k (name v)]))))
            (when title
              (h/title title))
            (h/link :rel "icon" :href (or icon empty-icon-url))
            (h/for-tpl [f fonts]   (h/style f))
            (h/for-tpl [s styles]  (h/link :rel "stylesheet" :href s))
            (h/for-tpl [s scripts] (h/script :src s)))))))

(defn dock [ctor]
  "fix the element to the window."
  (fn [{:keys [xl xr xt xb] :as attrs} elems]
    ;{:pre [(docks? xl xr xt xb)]} ;; todo: warn about pct w, pct h
    (with-let [e (ctor (dissoc attrs :xl :xr :xt :xb) elems)]
      (bind-in! e [out .-style .-position] (cell= (if (or xl xr xt xb) :fixed :initial)))
      (bind-in! e [out .-style .-zIndex]   (cell= (if (or xl xr xt xb) "9999" :initial)))
      (bind-in! e [out .-style .-left]     (cell= (or xl nil)))
      (bind-in! e [out .-style .-right]    (cell= (or xr nil)))
      (bind-in! e [out .-style .-top]      (cell= (or xt nil)))
      (bind-in! e [out .-style .-bottom]   (cell= (or xb nil))))))

;;; element primitives ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def leaf (comp shadow round border size #_dock fontable color transform clickable))
(def node (comp align pad gutter leaf))

;;; element primitives ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def elem    (-> h/div      box                                     node            parse-args))
(def cmpt*   (-> h/div      box interactive              toggleable node            parse-args))
(def canvas  (-> h/div      box (underlay h/canvas)                 node            parse-args))
(def frame   (-> h/div      box (underlay h/iframe)      frameable  node            parse-args))
(def image   (-> h/div      box (underlay h/img)         imageable  node            parse-args))
(def object  (-> h/div      box (underlay h/html-object) objectable node            parse-args))
(def video   (-> h/div      box (underlay h/video)       videoable  node            parse-args))
(def window* (->            doc                                     node windowable parse-args))

(def form*   (-> h/form     box         formable                    node            parse-args))
(def line    (-> h/input    box destyle fieldable   line-field      node            parse-args))
(def lines   (-> h/textarea box destyle fieldable   lines-field     node            parse-args))
(def pick    (-> h/div      box destyle fieldable   pick-field      node            parse-args))
(def picks   (-> h/div      box destyle fieldable   picks-field     node            parse-args))
(def item*   (-> h/option   box destyle interactive selectable  item-field node     parse-args))
(def file    (-> h/div      box         fieldable   file-field      node            parse-args))
(def files   (-> h/div      box         fieldable   file-field      node            parse-args))
(def write   (-> h/input    box destyle             send-field      node            parse-args))


; =========== add+ components ============

(defn cant-submit [data]
  (js/console.debug "No :submit action specified; can't submit:" data)
  data)

(defn formable+
  "Set up a form context."
  [ctor]
  (fn [{:keys [change submit default] :as attrs} elems]
    (let [default-cell (cell= (clean default))
          reset-data! (partial reset! *data*)
          submit! #(reset-data! (or (-> @*data* clean not-empty submit)
                                    @default-cell))]

      ; Form-level submit action for submit fields to use as default action
      (reset! *submit* (if submit submit! cant-submit))

      ; Changes in default values resets all form *data*
      (cell= (reset-data! default-cell))

      ; Monitor form *data* changes real-time
      (when change
        (cell= (change (clean *data*))))

      (with-let [e (ctor (dissoc attrs :change :submit :default) elems)]
        ; Pressing Enter submits the form
        (->> (bound-fn [e]
               (when (= (.-which e) 13)
                 ;(.preventDefault e)                        ; FIXME Why was this needed?
                 (@*submit*)))
             (.addEventListener (in e) "keypress"))))))

(defn fieldable+
  "Set the properties common to all form inputs."
  [ctor]
  (fn [{:keys [key val req autofocus] :as attrs} elems]
    (let [ks (cell= (if (vector? key) key [key]))]
      (with-let [e (ctor (dissoc attrs :key :val :req :autofocus :debounce) elems)]
        (let [field (in e)
              field-val #(.-value field)
              save (bound-fn [_]
                     (when *data*
                       (swap! *data* assoc-in
                              (read-string (.-name field))
                              (not-empty (field-val)))))
              debounced-save (if-let [deb (:debounce attrs)]
                               (debounce deb save)
                               save)
              sync-field-val (fn [new-data]
                               "Set field value from form data, but only if it's different.

                                The cursor jumps to the end of text input fields when setting their value,
                                even if setting it to the same value as its current value."
                               (let [new-field-val (get-in new-data @ks)]
                                 (when (not= (.-value field) new-field-val)
                                   (set! (.-value field) new-field-val))))]
          (.addEventListener field "change" save)
          (.addEventListener field "keyup" debounced-save)

          (when key
            ; When an input field is part of a form
            ; and form *data* changes for this input field
            ; then update the field value if necessary
            (cell= (sync-field-val *data*))

            ; TODO Per-field default value...
            #_(swap! *data* assoc-in @ks (or val (field-val))))

          (bind-in! e [in .-name] (cell= (pr-str ks)))
          (bind-in! e [in .-required] (cell= (when req :required)))
          (bind-in! e [in .-autofocus] autofocus))))))

(defn html-coll-seq
  "Returns a js/HTMLCollection as a lazy sequence"
  [html-coll]
  (for [i (range (.-length html-coll))]
    (-> html-coll (.item i))))

(defn selected-values
  "Returns the set of selected OPTION values of a SELECT js/Element."
  [select-field]
  (let [multi? (.-multiple select-field)
        maybe-set #(if multi? (into #{} %) (first %))
        sel-opts (.-selectedOptions select-field)]
    (->> sel-opts html-coll-seq
        (map #(-> % .-value read-string))
        (maybe-set)
        (#(do (pr %) %)))))

(defn sync-changed-options! [field new-selection]
  (doseq [opt (->> field .-options html-coll-seq)]
    (let [opt-val (read-string (.-value opt))
          selected-now? (.-selected opt)
          should-select? (contains? new-selection opt-val)]
      (when (not= should-select? selected-now?)
        (set! (.-selected opt) should-select?)))))

(defn select-field+ [ctor]
  (fn [{:keys [key multi? req autofocus rows size] :as attrs} option-kids]
    (let [ks (cell= (if (vector? key) key [key]))]
      (with-let [e (ctor (dissoc attrs :key :multi? :req :autofocus :debounce :rows :size) option-kids)]
        (let [field (in e)
              save (bound-fn [_]
                     (when *data*
                       (swap! *data* assoc-in @ks
                              ((if multi? not-empty identity)
                                (selected-values field)))))
              debounced-save (if-let [deb (:debounce attrs)]
                               (debounce deb save)
                               save)]
          (.addEventListener field "change" save)
          (.addEventListener field "keyup" debounced-save)

          (when key
            ; When a SELECT field is a child of a form
            ; and form *data* changes for its key-sequence
            ; then sync the selected property of its OPTION elements
            (cell= (->> (get-in *data* ks)
                        ((if multi? identity hash-set))
                        (sync-changed-options! field))))

          (bind-in! e [in .-name] (cell= (pr-str ks)))
          (bind-in! e [in .-multiple] (cell= (when multi? :multiple)))
          (bind-in! e [in .-required] (cell= (when req :required)))
          (bind-in! e [in .-autofocus] autofocus))))))

(defn toggleable+ [ctor]
  (fn [{:keys [key val req] :as attrs} elems]
    (let [data *data*
          key-vec (if (vector? key) key [key])]
      (swap! *data* assoc-in key-vec (or val false))
      (with-let [e (ctor (dissoc (merge {:s (em 1)} attrs) :key :val :req) elems)]
        (.addEventListener
          (in e) "change"
          #(when data (swap! data assoc-in key-vec
                             (.-checked (in e)))))
        (bind-in! e [in .-type] "checkbox")
        (bind-in! e [in .-name] (cell= (pr-str key)))
        (bind-in! e [in .-required] req)
        (bind-in! e [in .-checked] val)))))

(defn radioable+ [ctor]
  (fn [{:keys [key val req] :as attrs} elems]
    (let [ks (cell= (if (vector? key) key [key]))
          val (cell= val)]
      (with-let [e (ctor (dissoc (merge {:s (em 1)} attrs) :key :val :req) elems)]
        (let [field (in e)
              save (bound-fn [_]
                     (when (and *data* (.-checked field))
                       (swap! *data* assoc-in @ks @val)))
              sync-field-val (fn [new-data]
                               (let [new-field-val (get-in new-data @ks)
                                     new-field-checked? (= @val new-field-val)]
                                 (when (not= new-field-checked? (.-checked field))
                                   (set! (.-checked field) new-field-checked?))))]
          (.addEventListener field "change" save)
          (when key (cell= (sync-field-val *data*)))

          (bind-in! e [in .-type] "radio")
          (bind-in! e [in .-name] (cell= (pr-str key)))
          (bind-in! e [in .-required] (cell= (when req :required)))
          (bind-in! e [in .-value] (cell= (pr-str val))))))))

(defn send-field+ [ctor]
  (fn [{label :label submit :submit :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :label :submit) elems)]
      (->> (bound-fn [_]
             (if submit
               (reset! *data* (-> @*data* clean not-empty submit))
               (@*submit*)))
           (.addEventListener (mid e) "click"))
      (bind-in! e [in .-type] "button")
      (bind-in! e [in .-value] label))))

(def form*+   (-> h/form     box         formable+                    node            parse-args))
(def line+    (-> h/input    box destyle fieldable+   line-field      node            parse-args))
(def write+   (-> h/input    box destyle send-field+                  node            parse-args))

(def label+   (-> h/label    box destyle                              node            parse-args))
(def toggle   (-> h/input    box destyle toggleable+                  node            parse-args))
(def radio+   (-> h/input    box destyle radioable+                   node            parse-args))
(def select+  (-> h/select   box destyle select-field+                node            parse-args))

;;; utilities ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn b
  "breakpoints."
  [x & xs]
  (with-let [v (cell nil)]
    (let [[o vs] (case x :h ["width" xs] :v ["height" xs] ["width" (cons x xs)])]
      (doseq [[min val max] (partition 3 2 (concat [0] vs [999999]))]
        (let [query (.matchMedia js/window (str "(min-" o ": " min "px) and (max-" o ": " max "px)"))
              value! #(when (.-matches %) (set-cell!= v val))]
          (value! query)
          (.addListener query #(value! %)))))))

(defn s
  "states"
  ;; todo: transition between states
  [& kvs]
  (cell= ((apply hash-map kvs) *state*)))

(defn font
  "font"
  [family style weight names urls & [ranges]]
  {:pre [(v/family? family) (v/style? style) (v/weight? weight)]}
  (let [name  #(str "local('" % "')")
        url   #(str "url('" % "') format('" (second (re-find #".+\.([^?]+)(\?|$)" %)) "')")
        src   (apply str (interpose "," (concat (map name names) (map url urls))))
        range (when ranges (apply str (interpose "," ranges)))
        props {"font-family"   family ;; ->elem
               "font-style"    (when style  (clojure.core/name style))
               "font-weight"   (when weight (clojure.core/name weight))
               "src"           src
               "unicode-range" range}]
    (str "@font-face{" (apply str (mapcat (fn [[k v]] (str k ":" v ";")) (clean props))) "}")))

;;; markdown ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def f
 {1 32
  2 24
  3 20
  4 16
  5 14
  6 13})

(defmulti  md (fn [tag ats elems] tag))
(defmethod md :default    [tag ats elems] (elem elems))
(defmethod md :markdown   [_ ats elems] elems)
(defmethod md :header     [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :bulletlist [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :numberlist [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :listitem   [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :para       [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :code_block [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :inlinecode [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :img        [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :linebreak  [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :link       [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :link_ref   [_ {:keys [level]} elems] (elem :sh (r 1 1) :f (f level 16) elems))
(defmethod md :em         [_ {:keys [level]} elems] (elem :fi :italic                 elems))
(defmethod md :strong     [_ {:keys [level]} elems] (elem :ft :bold                   elems))

(defn parse [mdstr]
  (js->clj (.parse js/markdown mdstr) :keywordize-keys true))

(defn emit [[tag atr & nodes]]
  (let [[atr nodes] (if (map? atr) [atr nodes] [nil (cons atr nodes)])]
    #_(prn :mdtag tag :mdats atr :nodes nodes)
    (md (keyword tag) atr (mapv #(if (vector? %) (emit %) %) nodes))))

(defn markdown [mdstr]
  (emit (parse mdstr)))

;;; todos ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; offset (use outer css margin to move out of current position)
;; baseline-shift
;; background, url (str "url(" v ") no-repeat 50% 50% / cover")
;; update, previously implemented on do multimethod, to form middleware
;; throw proper ui exceptions with stack traces and attribute kv information
;; consider utility of introducing rtl positioning
