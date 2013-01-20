(ns todo.widgets
  (:use-macros [widje.macros :only [defwidget listen]])
  (:use [widje.core :only [bound bound* bound-coll]]
        [jayq.util :only [log]]
        [jayq.core :only [document-ready]])
  (:require [todo.core :as core]
            [todo.todos :as todos]
            [crate.core :as crate]
            [widje.core :as widje]
            [widje.util :as wu]))

;; Utils

(defn capitalize [string]
  (str (.toUpperCase (.charAt string 0))
    (.slice string 1)))

(defn pluralize [word count]
  (str word (when (> count 1) "s")))

(defn focus-delayed [input]
  (js/setTimeout #(.focus input) 0))

;; Widgets

(defwidget toggle-all* [todo-list]
  [:div
    (wu/bound-checkbox "toggle-all" "-toggle" todo-list (partial every? :completed?))
    [:label {:for "toggle-all"} "Mark all as complete"]]
  [toggle]
  (listen toggle :click
    (todos/check-all! (wu/checkbox-checked? toggle))))

(defn- todo-class [t editing]
  (str
    "-todo"
    (when (:completed? t) " completed")
    (when (= t editing) " editing")))

(defwidget todo* [t edt]
  [:li {:class (bound* [t edt] todo-class)}
    [:div.view
      (wu/bound-checkbox "" "toggle -toggle" t :completed?)
      [:label (bound t :title)]
      [:button.destroy.-destroy]]
    [:input.edit.-input {:value (bound t :title)}]]

  [todo toggle destroy input]
  (listen toggle :click
    (todos/check! @t (wu/checkbox-checked? toggle)))
  (listen destroy :click
    (todos/clear! @t))
  (listen todo :dblclick
    (core/edit-todo! @t))
  (listen input :blur
    (todos/save! @t (.val input))
    (core/quit-editing!))
  (listen input :keypress
    (when (= :enter (wu/evt->key event))
      (todos/save! @t (.val input))
      (core/quit-editing!)))
  (add-watch edt (gensym "focus-editing")
    #(when (= %4 @t) (focus-delayed input))))

(defn hide-if [cond]
  (if cond "display: none;" ""))

(defwidget todos* [todo-list editing-todo]
  [:section#main {:style (bound todo-list #(hide-if (empty? %)))}
    (toggle-all* todo-list)
    [:ul#todo-list (bound-coll todo-list {:as #(todo* % editing-todo)})]])

(defwidget new-todo* []
  [:input#new-todo.-input {:placeholder "What needs to be done?" :autofocus true}]
  [input]
  (listen input :keypress
    (when (= :enter (wu/evt->key event))
      (todos/add! (.val input))
      (.val input ""))))

(defwidget filters* [current-filter]
  [:ul#filters
    (for [filter [:all :active :completed]]
      [:li [:a {:class (bound current-filter #(if (= filter %) "selected" ""))
                :href (str "#/" (name filter))}
             (capitalize (name filter))]])])

(defwidget clear-button* [stats]
  [:button#clear-completed.-button {:style (bound stats #(hide-if (zero? (:completed %))))}
    (bound stats #(str "Clear completed (" (:completed %) ")"))]
  [button]
  (listen button :click
    (todos/clear-completed!)))

(defwidget footer* [filter stats]
  [:footer#footer {:style (bound stats #(hide-if (zero? (:all %))))}
    [:span#todo-count
      [:b (bound stats :active)]
      [:span (bound stats #(str " " (pluralize "item" (:active %)) " left"))]]
    (filters* filter)
    (clear-button* stats)])

(defwidget page* []
  [:div
    [:header#header
      [:h1 "todos"]
      (new-todo*)]
    (todos* core/!visible-todos core/!editing-todo)
    (footer* core/!filter core/!stats)])

;; Init

(document-ready #(widje/render "#todoapp" page*))
