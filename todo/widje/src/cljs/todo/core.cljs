(ns todo.core
  (:use [cljs.reader :only [read-string]]
        [clojure.string :only [blank?]]
        [jayq.util :only [log]]))

;;;;;;;;;;;;;;;;;;;;;
;;Core application state

(def !todos
  "Todo list, implicitly key'd by :title"
  (atom []))

(def !filter
  "Which todo items should be displayed: all, active, or completed?"
  (atom :all))

(def !visible-todos
  "Filtered todo list"
  (atom []))

(def !stats
  "Numbers of todos by complete status"
  (atom {}))

(def !editing
  "Currently edited item"
  (atom nil))

;;;;;;;;;;;;;;;;;;;;;
;;"Routing"

(defn update-filter!
  "Updates filter according to current location hash"
  []
  (let [[_ loc] (re-matches #"#/(\w+)" (.-hash js/location))]
    (reset! !filter (keyword loc))))

(set! (.-onhashchange js/window) update-filter!)


;;;;;;;;;;;;;;;;;;;
;;Persistence

(def ls-key "todos-widje")

(defn save-todos! [_ _ _ todos]
  (aset js/localStorage ls-key
        (prn-str todos)))

(defn load-todos! []
  (reset! !todos
          (if-let [saved-str (aget js/localStorage ls-key)]
            (read-string saved-str)
            [])))

(add-watch !todos :save save-todos!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Query/manipulate todos

(defn filter-todos [filter todos]
  (reset! !visible-todos
    (case filter
      :active    (remove :completed? todos)
      :completed (filter :completed? todos)
      todos)))

(add-watch !todos  :filter-todos #(filter-todos @!filter %4))
(add-watch !filter :filter-todos #(filter-todos %4 @!todos))

(defn calc-stats [_ _ _ todos]
  (reset! !stats {:all (count todos)
                  :completed (count (filter :completed? todos))
                  :active (count (remove :completed? todos))}))

(add-watch !todos :calc-stats calc-stats)

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

(defn edit-todo!
  "Mark an item as currently being editing"
  [todo]
  (reset! !editing todo))

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
    (replace-todo! todo (assoc todo :title title)))
  (reset! !editing nil))
