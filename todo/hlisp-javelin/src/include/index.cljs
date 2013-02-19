(ns todo
  (:require-macros
    [hlisp.macros                 :refer [tpl]]
    [hlisp.reactive.macros        :refer [reactive-attributes]]
    [tailrecursion.javelin.macros :refer [cell]])
  (:require
    [hlisp.reactive         :as d :refer [thing-looper]]
    [hlisp.util                   :refer [pluralize]]
    [tailrecursion.javelin        :refer [route*]]
    [alandipert.storage-atom      :refer [local-storage]]))

;; internal ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare route state active? deleted? completed?)

(defn- update-state! [todos]
  (let [grouped (group-by deleted? todos)]
    (reset! state (into (get grouped false) (get grouped true)))))

(defn- reactive-info [todos i]
  (let [cellval #(cell (get (nth todos i) %))]
    (conj (mapv cellval [:editing :completed :text])
          (cell (let [todo (nth todos i)]
                  (and (not (deleted? todo)) 
                       (or (= "#/" route)
                           (and (= "#/active" route) (active? todo))
                           (and (= "#/completed" route) (completed? todo)))))))))

(def deleted?     #(empty? (:text (if (map? %) % (nth @state %)))))
(def completed?   #(:completed (if (map? %) % (nth @state %))))
(def active?      #(and (not (deleted? %)) (not (completed? %))))
(def clear        #(assoc % :editing false :completed false :text "")) 
(def dfl-state    (vec (repeat 50 {:editing false :completed true :text ""}))) 

;; public ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def state        (local-storage (cell dfl-state) ::store))
(def route        (route* 50 "#/")) 
(def editing-new  (cell "")) 
(def live-ones    (cell (filter (complement deleted?) state))) 
(def completed    (cell (filter completed? live-ones))) 
(def active       (cell (filter (complement completed?) live-ones)))
(def loop-todos   (thing-looper state reactive-info)) 

(defn new-item! [text]
  (if (not (empty? text))
    (update-state! (into [(assoc (clear (peek @state)) :text text)] (pop @state)))))

(defn set-editing! [i v]
  (update-state! (-> (mapv #(assoc % :editing false) @state)
                     (assoc-in [i :editing] v))))

(defn set-completed! [i v]
  (update-state! (assoc-in @state [i :completed] v)))

(defn set-all-completed! [v]
  (update-state! (mapv #(assoc % :completed v) @state)))

(defn set-text! [i v]
  (update-state! (assoc-in (vec @state) [i :text] v)))

(defn clear-completed! []
  (update-state! (mapv #(if (completed? %) (clear %) %) @state)))
