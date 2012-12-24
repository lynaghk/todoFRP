(ns todo.list
  (:use-macros [c2.util :only [p pp bind!]])
  (:use [c2.core :only [unify]])
  (:require [todo.core :as core]
            [c2.dom :as dom]
            [c2.event :as event]
            [clojure.string :as  str]))


(defn todo*
  "Todo item template"
  [t]
  (let [{:keys [completed? title editing?]} t]
    [:li {:class (str/join " " [(when completed? "completed")
                                (when editing? "editing")])}
     [:div.view
      [:input.toggle {:type "checkbox"
                      :properties {:checked completed?}}]
      [:label title]
      [:button.destroy]]
     [:input.edit {:value title}]]))

(bind! "#main"
       [:section#main {:style {:display (when (zero? (core/todo-count)) "none")}}
        [:input#toggle-all {:type "checkbox"
                            :properties {:checked (every? :completed? @core/!todos)}}]
        [:label {:for "toggle-all"} "Mark all as complete"]
        [:ul#todo-list (unify (case @core/!filter
                                :active    (remove :completed? @core/!todos)
                                :completed (filter :completed? @core/!todos)
                                ;;default to showing all events
                                @core/!todos)
                              todo*)]])

;;If the application state changes, check to see if it's because a todo is being edited.
;;If so, focus on that input element.
(add-watch core/!todos :focus-editing
           (fn []
             (if-let [$input (dom/select ".editing input.edit")]
               (.focus $input))))

(bind! "#footer"
       [:footer#footer {:style {:display (when (zero? (core/todo-count)) "none")}}

        (let [items-left (core/todo-count false)]
          [:span#todo-count
           [:b items-left]
           (str " item" (if (= 1 items-left) "" "s") " left")])

        [:ul#filters
         (unify [:all :active :completed]
                (fn [type]
                  [:li
                   [:a {:class (if (= type @core/!filter) "selected" "")
                        :href (str "#/" (name type))}
                    (core/capitalize (name type))]])
                :force-update? true)]


        [:button#clear-completed
         {:style {:display (when (zero? (core/todo-count true)) "none")}}
         "Clear completed (" (core/todo-count true) ")"]])


;;;;;;;;;;;;;;;;;;;;;
;;Todo event handlers

(event/on "#todo-list" ".toggle" :click
          (fn [d _ e]
            (let [checked? (.-checked (.-target e))]
              (core/check-todo! d checked?))))

(event/on "#todo-list" ".destroy" :click
          (fn [d] (core/clear-todo! d)))



;;Editing
(event/on "#todo-list" :dblclick
          (fn [d] (core/edit-todo! d)))

(let [edit-todo! (fn [d e]
                   (let [new-title (dom/val (.-target e))]
                     (if (= "" new-title)
                       (core/clear-todo! d)
                       (core/replace-todo! d
                                           (-> d
                                               (assoc :title new-title)
                                               (dissoc :editing?))))))]

  (event/on "#todo-list" ".edit" :blur
            (fn [d _ e]
              (edit-todo! d e))
            ;;Blur events don't bubble up the DOM, so we need to tell the listener to grab 'em in the capture phase
            :capture true)

  (event/on "#todo-list" ".edit" :keypress
            (fn [d _ e]
              (when (= :enter (core/evt->key e))
                (edit-todo! d e)))))


;;;;;;;;;;;;;;;;;;;;;;;;
;;Control event handlers

(event/on-raw "#toggle-all" :click
              (fn [e]
                (let [checked? (.-checked (.-target e))]
                  (core/check-all! checked?))))

(event/on-raw "#clear-completed" :click
              core/clear-completed!)

(let [$todo-input (dom/select "#new-todo")]
  (event/on-raw $todo-input :keypress
                (fn [e]
                  (when (= :enter (core/evt->key e))
                    (core/add-todo! (dom/val $todo-input))
                    (dom/val $todo-input "")))))
