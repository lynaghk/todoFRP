(ns todo
  (:require-macros
    [hlisp.macros                 :refer [tpl]]
    [hlisp.reactive.macros        :refer [reactive-attributes]]
    [tailrecursion.javelin.macros :refer [cell]])
  (:require
    [clojure.string         :as s :refer [blank?]]
    [hlisp.reactive         :as d :refer [thing-looper]]
    [hlisp.util                   :refer [pluralize]]
    [tailrecursion.javelin        :refer [route*]]
    [alandipert.storage-atom      :refer [local-storage]]))

;; internal ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare route completed?)

(defn- reactive-info [todos i]
  (let [todo (cell (get-in todos [i]))]
    [(cell (:editing todo))
     (cell (:completed todo))
     (cell (:text todo))
     (cell (or (= "#/" route)
               (and (= "#/active" route) (not (completed? todo)))
               (and (= "#/completed" route) (completed? todo))))]))

;; public ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def completed?   #(:completed %))
(def state        (local-storage (cell '[]) ::store))
(def route        (route* 50 "#/")) 
(def editing-new  (cell "")) 
(def completed    (cell (filter completed? state))) 
(def active       (cell (remove completed? state)))
(def loop-todos   (thing-looper state reactive-info)) 

(defn add-item! [text]
  (if-not (blank? text)
    (let [todo {:editing false :completed false :text text}]
      (swap! state #(into [todo] (vec %))))))

(defn remove-item! [i]
  (swap! state #(into (subvec % 0 i) (subvec % (inc i)))))

(defn set-editing! [i v]
  (swap! state (fn [s] (-> (mapv #(assoc % :editing false) s)
                         (assoc-in [i :editing] v)))))

(defn set-text! [i v]
  (when-not (blank? v) (swap! state assoc-in [i :text] v)))

(defn set-completed! [i v]
  (swap! state assoc-in [i :completed] v))

(defn set-all-completed! [v]
  (swap! state (fn [s] (mapv #(assoc % :completed v) s))))

(defn clear-completed! []
  (swap! state #(vec (remove completed? %))))
