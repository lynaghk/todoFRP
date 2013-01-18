(ns todo.core
  (:use [clojure.string :only [blank?]]
        [jayq.util :only [log]]
        [jayq.core :only [document-ready]])
  (:require [todo.todos :as todos]))

;; View state

(def !filter
  "Which todo items should be displayed: all, active, or completed?"
  (atom :all))

(def !visible-todos
  "Filtered todo list"
  (atom []))

(defn filter-todos [fltr todos]
  (reset! !visible-todos
    (vec (case fltr
           :active    (remove :completed? todos)
           :completed (filter :completed? todos)
           :all todos))))

(add-watch todos/!list :filter-todos #(filter-todos @!filter %4))
(add-watch !filter     :filter-todos #(filter-todos %4 @todos/!list))

(def !editing-todo
  "Currently edited item"
  (atom nil))

(def !stats
  "Numbers of todos by complete status"
  (atom {}))

(add-watch todos/!list :calc-stats
  #(reset! !stats {:all (count %4)
                   :completed (count (filter :completed? %4))
                   :active (count (remove :completed? %4))}))

;; View state operations

(defn edit-todo!
  "Mark an item as currently being editing"
  [todo]
  (reset! !editing-todo todo))

(defn quit-editing!
  "Finish editing"
  []
  (reset! !editing-todo nil))

;; Routing

(defn update-filter!
  "Updates filter according to current location hash"
  []
  (let [[_ loc] (re-matches #"#/(\w+)" (.-hash js/location))
        loc (if (blank? loc) :all (keyword loc))]
    (when-not (= @!filter loc)
      (reset! !filter loc))))

(set! (.-onhashchange js/window) update-filter!)

;; Init

(document-ready update-filter!)
