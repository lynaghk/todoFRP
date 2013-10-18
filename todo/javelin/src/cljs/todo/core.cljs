(ns todo.core
  (:require [clojure.browser.event :as event]
            [clojure.string :as string]
            [tailrecursion.javelin]
            [todo.utils :as utils]
            [dommy.core :as dommy])
  (:use [cljs.reader :only [read-string]]
        [domina :only [add-class! by-id remove-class! set-text! set-html! swap-content! single-node]]
        [domina.css :only [sel]]
        [domina.events :only [capture! listen!]]
        [domina.xpath :only [xpath]])
  (:require-macros [tailrecursion.javelin :refer [defc defc= cell=]]
                   [dommy.macros :refer [node]]))

(def ENTER-KEY 13)

;; Filter functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-all-items [items] (identity items))

(defn get-active-items [items]
  (into (vector) (remove #(:completed %) items)))

(defn get-completed-items [items]
  (into (vector) (filter #(:completed %) items)))

;; todo reactors ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defc todo-id   0)
(defc todos     [])
(defc filter-fn get-all-items)

(defc= filtered-todos  (filter-fn todos))
(defc= active-items    (get-active-items todos))
(defc= completed-items (get-completed-items todos))

;; Functions for todo list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new! [text]
  (let [text (string/trim text)]
    (when-not (empty? text)
      (swap! todos conj {:id @todo-id :completed false :text text})
      (swap! todo-id inc))))

(defn replace! [old new]
  (swap! todos #(replace {old new} %)))

(defn mark-all! [flag]
 (swap! todos #(mapv (fn [x] (assoc x :completed flag)) %)))

(defn delete! [item]
  (swap! todos #(into (vector) (remove (fn [x] (= x item)) %))))

(defn completed-all? [items]
  (= (count items) (count @completed-items)))

;; UI functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare todo-template)

(defn add-new-todo []
  (new! (.-value (by-id "new-todo")))
  (aset (by-id "new-todo") "value" ""))

(defn show-todos [items]
  (-> (sel "#todo-list")
      (swap-content! (todo-template items))))

(defn update-completed-count [items]
  (let [node (sel "#clear-completed")]
    (if (> (count items) 0)
      (do (set-text! node (str "Clear completed (" (count items) ")"))
          (remove-class! node "hidden"))
      (add-class! node "hidden"))))

(defn update-filter-text [url]
  (-> (xpath "//ul[@id='filters']//a")
      (remove-class! "selected"))
  (-> (xpath (format "//ul[@id='filters']//a[@href='#/%s']" url))
      (add-class! "selected")))

(defn todo-listeners
  "Event liseners for each todo item"
  [node todo]

  ;; mark completed item
  (listen! (sel node ".toggle") :click
           #(replace! todo (assoc todo :completed (not (get todo :completed)))))

  ;; editing item
  (listen! (sel node "label") :dblclick
           #(do (add-class! (sel node) "editing")
                (.focus (single-node (sel node ".edit")))))

  ;; finish editing
  (let [finish-editing #(let [text (.-value (:target %))]
                          (if (empty? (string/trim text))
                            (delete! todo)
                            (replace! todo (assoc todo :text text)))
                          (remove-class! (sel node) "editing"))]
    (capture! (sel node ".edit") :blur #(finish-editing %))
    (listen! (sel node ".edit") :keypress #(if (= ENTER-KEY (:keyCode %))
                                             (finish-editing %))))
  ;; delete item
  (listen! (sel node ".destroy") :click #(delete! todo)))


(defn todo-template [items]
  (node [:ul#todo-list
         (for [todo (reverse items)]
           (let [dom (node [:li ^:attrs (if (:completed todo) {:class "completed"})
                            [:div.view
                             [:input.toggle
                              ^:attrs (merge {:type "checkbox"} (if (:completed todo) {:checked ""}))]
                             [:label (:text todo)]
                             [:button.destroy]]
                            [:input.edit {:value (:text todo)}]])]
             (todo-listeners dom todo)
             dom))]))

;; localstorage ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ls-key "todos-javelin")
(defn save-todos! [todos]
  (aset js/localStorage ls-key
        (prn-str todos)))

(defn load-todos! []
  (reset! todos
          (if-let [saved-str (aget js/localStorage ls-key)]
            (read-string saved-str)
            [])))

;; navigation handlers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn nav-handler [{:keys [token type navigation?]}]
  (when navigation?
    (case (name token)
      "active" (do (reset! filter-fn get-active-items)
                   (update-filter-text "active"))
      "completed" (do (reset! filter-fn get-completed-items)
                      (update-filter-text "completed"))
      (do (reset! filter-fn get-all-items)
          (update-filter-text "")))))

(def history (utils/history nav-handler))

(defn ^:export start []
  (load-todos!)
  (cell= (save-todos! todos))
  ;; update the main and footer sections ;;;;;;;;;;;;;;;;;;;;;;;
  (cell= (utils/toggle-class! (sel "#main") "hidden" (empty? todos)))
  (cell= (utils/toggle-class! (sel "#footer") "hidden" (empty? todos)))

  ;; update the UI here ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (cell= (show-todos filtered-todos))
  (cell= (set-html! (sel "#todo-count")
                    (let [active-num (count active-items)]
                      (.-outerHTML (node [:span#todo-count
                                          [:strong (str active-num)]
                                          (str " item" (if-not (= active-num 1) "s") " left")])))))
  (cell= (update-completed-count completed-items))
  (cell= (let [node (by-id "toggle-all")
               result (completed-all? todos)]
           (aset node "checked" result)))
  
  ;; event listeners ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (listen! (sel "#new-todo") :keypress #(if (= ENTER-KEY (:keyCode %))
                                          (add-new-todo)))
  (listen! (sel "#toggle-all") :click #(mark-all! (.-checked (by-id "toggle-all"))))
  (listen! (sel "#clear-completed") :click #(doseq [item @completed-items]
                                              (delete! item))))
