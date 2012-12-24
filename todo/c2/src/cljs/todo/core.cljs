(ns todo.core
  (:use-macros [c2.util :only [p pp]])
  (:use [cljs.reader :only [read-string]]
        [clojure.string :only [blank?]]))

;;;;;;;;;;;;;;;;;;;;;
;;Core application state

(def !todos
  "Todo list, implicitly key'd by :title"
  (atom []))

(def !filter
  "Which todo items should be displayed: all, active, or completed?"
  (atom :all))


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

(def ls-key "todos-c2")
(defn save-todos! []
  (aset js/localStorage ls-key
        (prn-str (map #(dissoc % :editing?) ;;Don't save editing state
                      @!todos))))

(defn load-todos! []
  (reset! !todos 
          (if-let [saved-str (aget js/localStorage ls-key)]
            (read-string saved-str)
            [])))

(add-watch !todos :save save-todos!)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Query/manipulate todos

(defn todo-count
  ([] (count @!todos))
  ([completed?] (count (filter #(= completed? (% :completed?))
                               @!todos))))

(defn clear-completed!
  "Remove completed items from the todo list."
  []
  (swap! !todos #(remove :completed? %)))

(defn replace-todo! [old new]
  (swap! !todos #(replace {old new} %)))

(defn check-todo!
  "Mark an item as (un)completed."
  [todo completed?]
  (replace-todo! todo (assoc todo :completed? completed?)))

(defn edit-todo!
  "Mark an item as currently being editing"
  [todo]
  (replace-todo! todo (assoc todo :editing? true)))

(defn check-all!
  "Mark all items as (un)completed."
  [completed?]
  (swap! !todos (fn [todos]
                  (map #(assoc % :completed? completed?)
                       todos))))

(defn title-exists?
  "Is there already a todo with that title?"
  [title]
  (some #(= title %)
        (map :title @!todos)))

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
  (swap! !todos
         (fn [todos]
           (remove #(= todo %) todos))))


;;;;;;;;;;;;;;;;;;;;;
;;Lil' helpers

(defn capitalize [string]
  (str (.toUpperCase (.charAt string 0))
       (.slice string 1)))

(defn evt->key [e]
  (get {13 :enter} (.-keyCode e)))
