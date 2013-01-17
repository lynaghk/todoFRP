(ns todo.core
  (:use [cljs.reader :only [read-string]]
        [clojure.string :only [blank?]]
        [jayq.util :only [log]]))

;; State

(def !todos
  "Todo list, implicitly key'd by :title"
  (atom []))

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

(add-watch !todos  :filter-todos #(filter-todos @!filter %4))
(add-watch !filter :filter-todos #(filter-todos %4 @!todos))

;; Routing

(defn update-filter!
  "Updates filter according to current location hash"
  []
  (let [[_ loc] (re-matches #"#/(\w+)" (.-hash js/location))
        loc (if (blank? loc) :all (keyword loc))]
    (when-not (= @!filter loc)
      (reset! !filter loc))))

(set! (.-onhashchange js/window) update-filter!)

;; Persistence

(defn load-todos! []
  (reset! !todos
          (if-let [saved-str (aget js/localStorage "todos-widje")]
            (read-string saved-str)
            [])))

(add-watch !todos :save
  #(aset js/localStorage "todos-widje" (prn-str %4)))

;; Operations

(defn clear-completed!
  "Remove completed items from the todo list."
  []
  (swap! !todos (partial remove :completed?)))

(defn replace-todo! [old new]
  (swap! !todos (partial replace {old new})))

(defn check-todo!
  "Mark an item as (un)completed."
  [todo completed?]
  (replace-todo! todo (assoc todo :completed? completed?)))

(defn check-all!
  "Mark all items as (un)completed."
  [completed?]
  (swap! !todos (fn [todos]
                  (map #(assoc % :completed? completed?) todos))))

(defn title-exists?
  "Is there already a todo with that title?"
  [title]
  (some #(= title (:title %)) @!todos))

(defn add-todo!
  "Add a new todo to the list."
  [title]
  (let [title (.trim title)]
    (when (not (or (blank? title)
                   (title-exists? title)))
      (swap! !todos conj {:title title :completed? false}))))

(defn clear-todo!
  "Remove a single todo from the list."
  [todo]
  (swap! !todos (partial remove (partial = todo))))

(defn save-todo!
  "Save edited todo title"
  [todo title]
  (if (= "" title)
    (clear-todo! todo)
    (replace-todo! todo (assoc todo :title title))))
