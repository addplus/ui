(ns hoplon.ui.plus
  (:refer-clojure :exclude [binding bound-fn])
  (:require
    [hoplon.core :as h :refer [defelem for-tpl when-tpl if-tpl case-tpl]]
    [clojure.string :refer [blank? join split ends-with?]]
    [cljs.reader :refer [read-string]]
    [javelin.core :refer [cell cell?]]
    [hoplon.ui :refer [elem clean *data* *submit* throw-ui-exception bind-with debounce
                       autocapitalizes? autocompletes? contents? integers?
                       node parse-args destyle line-field vstr]]
    [hoplon.ui.attrs :refer [r em ratio? calc? ->attr]]
    [hoplon.ui.elems :refer [box doc out mid in elem?]]
    [hoplon.ui.validation :as v]
    [hoplon.binding]
    [cljsjs.markdown])
  (:require-macros
    [hoplon.core :refer [with-timeout]]
    [hoplon.binding :refer [binding bound-fn]]
    [hoplon.ui :refer [bind-in!]]
    [hoplon.ui.plus :refer [form]]
    [javelin.core :refer [cell= with-let set-cell!=]]))

(defn cant-submit [data]
  (js/console.debug "No :submit action specified; can't submit:" data)
  data)

(defn formable
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

(defn fieldable
  "Set the properties common to all form inputs."
  [ctor]
  (fn [{:keys [key val req autofocus] :as attrs} elems]
    (when val (throw-ui-exception "The :val attribute is NOT IMPLEMENTED YET"))
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

(defn lines-field [ctor]
  (fn [{:keys [rows cols autocomplete autocapitalize content prompt charsize charmin charmax resizable] :as attrs} elems]
    {:pre [(autocompletes? autocomplete) (autocapitalizes? autocapitalize) (contents? content) (integers? charsize charmin charmax)]}
    (with-let [e (ctor (dissoc attrs :rows :cols :autocomplete :autocapitalize :content :prompt :charsize :charmin :charmax :resizeable) elems)]
      (->> (bound-fn [e]
             (when (= (.-which e) 13)
               (.stopPropagation e)))
           (.addEventListener (in e) "keypress"))

      (bind-in! e [in .-style .-padding] "0")
      (bind-in! e [in .-rows] (cell= (if rows (str rows) "2")))
      (bind-in! e [in .-style .-height] (cell= (if rows nil "100%")))
      (bind-in! e [in .-cols] (cell= (when cols (str cols))))
      (bind-in! e [in .-style .-width] (cell= (if cols nil "100%")))
      (bind-in! e [in .-style .-resize] (cell= (or resizable :none)))
      (bind-in! e [in .-type] content)
      (bind-in! e [in .-placeholder] prompt)
      (bind-in! e [in .-autocomplete] autocomplete)
      (bind-in! e [in .-autocapitalize] autocapitalize)

      ;(bind-in! e [in .-size]           charsize)
      (bind-in! e [in .-minlength] charmin)
      (bind-in! e [in .-maxlength] charmax))))

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
         (maybe-set))))

(defn sync-changed-options! [field new-selection]
  (doseq [opt (->> field .-options html-coll-seq)]
    (let [opt-val (read-string (.-value opt))
          selected-now? (.-selected opt)
          should-select? (contains? new-selection opt-val)]
      (when (not= should-select? selected-now?)
        (set! (.-selected opt) should-select?)))))

(defn select-field [ctor]
  (fn [{:keys [key multi? req autofocus rows size] :as attrs} option-kids]
    (when (or rows size) (throw-ui-exception "Use :sv instead"))
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

(defn toggleable [ctor]
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

(defn radioable [ctor]
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

(defn send-field [ctor]
  (fn [{label :label submit :submit :as attrs} elems]
    (with-let [e (ctor (dissoc attrs :label :submit) elems)]
      (->> (bound-fn [_]
             (if submit
               (reset! *data* (-> @*data* clean not-empty submit))
               (@*submit*)))
           (.addEventListener (mid e) "click"))
      (bind-in! e [in .-type] "button")
      (bind-in! e [in .-value] label))))

(def form* (-> h/form box formable node parse-args))
(def line (-> h/input box destyle fieldable line-field node parse-args))
(def lines (-> h/textarea box destyle fieldable lines-field node parse-args))
(def write (-> h/input box destyle send-field node parse-args))

(def label (-> h/label box destyle node parse-args))
(def toggle (-> h/input box destyle toggleable node parse-args))
(def radio (-> h/input box destyle radioable node parse-args))
(def select (-> h/select box destyle select-field node parse-args))

(defelem row [attrs elems] (elem :sh (r 1 1) attrs elems))
(defelem container [attrs elems] (elem :s (r 1 1) attrs elems))