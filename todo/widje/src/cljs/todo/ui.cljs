(ns todo.ui
  (:use-macros [widje.macros :only [defwidget listen]])
  (:use [crate.binding :only [bound bound-coll]]
        [jayq.util :only [log]])
  (:require [todo.core :as core]
            [crate.core :as crate]
            [jayq.core :as jq]
            [widje.role :as role] ; todo remove this
            [widje.core :as widje]))

;; Utils

(defn capitalize [string]
  (str (.toUpperCase (.charAt string 0))
       (.slice string 1)))

(defn pluralize [word count]
  (str word (when (> count 1) "s")))

(defn evt->key [e]
  (get {13 :enter} (.-keyCode e)))

(deftype atoms-binding [atoms value-func]
  crate.binding/bindable
  (-value [this] (apply value-func (map deref atoms)))
  (-on-change [this func]
    (doseq [atm atoms]
      (add-watch atm (gensym "atom-binding") #(func (crate.binding/-value this))))))

(defn bound* [atoms & [func]]
  (let [func (or func identity)]
    (atoms-binding. atoms func)))

(defn checked? [checkbox]
  (.is checkbox ":checked"))

(defn check [checkbox value]
  (if value
    (jq/attr checkbox "checked" true)
    (jq/remove-attr checkbox "checked")))

(defn focus-delayed [input]
  (js/setTimeout #(.focus input) 0))

;; Widgets

(defwidget bound-checkbox [id classes atm fn]
  [:input.-checkbox {:id id
                     :class (str classes " -checkbox")
                     :type "checkbox"}]
  [checkbox]
  (check checkbox (fn @atm))
  (add-watch atm (gensym "bound-checkbox")
    #(check checkbox (fn %4))))


(defn- todo-class [t editing]
  (str
    "-todo"
    (when (:completed? t) " completed")
    (when (= t editing) " editing")))

(defwidget todo* [t]
  [:li {:class (bound* [t core/!editing] todo-class)}
    [:div.view
      (bound-checkbox "" "toggle -toggle" t :completed?)
      [:label (bound t :title)]
      [:button.destroy.-destroy]]
    [:input.edit.-input {:value (bound t :title)}]]
  
  [todo toggle destroy input]
  (listen toggle :click
    (core/check-todo! @t (checked? toggle)))
  (listen destroy :click
    (core/clear-todo! @t))
  (listen todo :dblclick
    (core/edit-todo! @t))
  (listen input :blur
    (core/save-todo! @t (.val input)))
  (listen input :keypress
    (when (= :enter (evt->key event))
      (core/save-todo! @t (.val input))))
  (add-watch core/!editing (gensym "focus-editing")
    #(when (= %4 @t) (focus-delayed input))))

(defwidget toggle-all* []
  [:div
    (bound-checkbox "toggle-all" "-toggle" core/!todos #(every? :completed? %))]
  [toggle]
  (listen toggle :click
    (core/check-all! (checked? toggle))))

(defwidget todos* []
  [:section#main {:style (bound core/!todos #(when-not (seq %) "display: none;"))}
    (toggle-all*)
    [:label {:for "toggle-all"} "Mark all as complete"]
    [:ul#todo-list (bound-coll core/!visible-todos {:as todo*})]])

(defwidget new-todo* []
  [:input#new-todo.-input {:placeholder "What needs to be done?" :autofocus true}]
  [input]
  (listen input :keypress
    (when (= :enter (evt->key event))
      (core/add-todo! (.val input))
      (.val input ""))))

(defwidget filters* []
  [:ul#filters
    (for [filter [:all :active :completed]]
      [:li [:a {:class (bound core/!filter #(when (= filter %) "selected"))
                :href (str "#/" (name filter))}
            (capitalize (name filter))]])]
  )

(defwidget clear-button* []
  [:button#clear-completed.-button {:style (bound core/!stats #(when (zero? (:completed %)) "display: none;"))}
    (bound core/!stats #(str "Clear completed (" (:completed %) ")"))]
  [button]
  (listen button :click
    (core/clear-completed!)))

(defwidget footer* []
  [:footer#footer {:style (bound core/!todos #(when-not (seq %) "display: none;"))}
    [:span#todo-count
      [:b (bound core/!stats :active)]
      [:span (bound core/!stats #(str " " (pluralize "item" (:active %)) " left"))]]
    (filters*)
    (clear-button*)])

(defwidget page* []
  [:div
    [:header#header
      [:h1 "todos"]
      (new-todo*)]
    (todos*)
    (footer*)])

(defn render []
  (widje/render "#todoapp" page*))