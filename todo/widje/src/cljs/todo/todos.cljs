(ns todo.todos
  (:use [cljs.reader :only [read-string]]
        [clojure.string :only [blank?]]
        [jayq.util :only [log]]
        [jayq.core :only [document-ready]]))

;; State

(def !list
  "Todo list, implicitly key'd by :title"
  (atom []))

;; Persistence

(defn load! []
  (reset! !list
    (if-let [saved-str (aget js/localStorage "todos-widje")]
      (read-string saved-str)
      [])))

(add-watch !list :save
  #(aset js/localStorage "todos-widje" (prn-str %4)))

;; Operations

(defn clear-completed!
  "Remove completed items from the todo list."
  []
  (swap! !list (partial remove :completed?)))

(defn replace! [old new]
  (swap! !list (partial replace {old new})))

(defn check!
  "Mark an item as (un)completed."
  [todo completed?]
  (replace! todo (assoc todo :completed? completed?)))

(defn check-all!
  "Mark all items as (un)completed."
  [completed?]
  (swap! !list (fn [todos]
                  (map #(assoc % :completed? completed?) todos))))

(defn title-exists?
  "Is there already a todo with that title?"
  [title]
  (some #(= title (:title %)) @!list))

(defn add!
  "Add a new todo to the list."
  [title]
  (let [title (.trim title)]
    (when (not (or (blank? title)
                 (title-exists? title)))
      (swap! !list conj {:title title :completed? false}))))

(defn clear!
  "Remove a single todo from the list."
  [todo]
  (swap! !list (partial remove (partial = todo))))

(defn save!
  "Save edited todo title"
  [todo title]
  (if (= "" title)
    (clear! todo)
    (replace! todo (assoc todo :title title))))

;; Init

(document-ready load!)