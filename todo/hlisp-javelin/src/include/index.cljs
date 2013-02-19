(ns todo
  (:require-macros
    [hlisp.macros                 :refer [tpl def-elem def-values <- <<-]]
    [hlisp.reactive.macros        :refer [reactive-attributes]]
    [tailrecursion.javelin.macros :refer [mirror cell mx]])
  (:require
    [hlisp.reactive               :as d]
    [hlisp.util                   :refer [pluralize]]
    [tailrecursion.javelin        :as j]
    [alandipert.storage-atom      :as sa]
    [hlisp.util                   :as hu]))

;; internal ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare route state active? deleted? completed?)

(defn- update-state! [todos]
  (let [grouped (group-by deleted? todos)]
    (reset! state (into (get grouped false) (get grouped true)))))

(defn- loop-things [things f g container]
  (into container (mapv #(apply f % (g things %)) (range 0 (count @things)))))

(defn- cellval [things i k]
  (cell (get (nth things i) k)))

(defn- cellvals [things i]
  (conj (mapv (partial cellval things i) [:editing :completed :text])
        (cell (let [thing (nth things i)]
                (and (not (deleted? thing)) 
                     (or (= "#/" route)
                         (and (= "#/active" route) (active? thing))
                         (and (= "#/completed" route) (completed? thing))))))))

(def deleted?     #(empty? (:text (if (map? %) % (nth @state %)))))
(def completed?   #(:completed (if (map? %) % (nth @state %))))
(def active?      #(and (not (deleted? %)) (not (completed? %))))

;; public ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def route (j/route* 50 "#/"))

(let [dfl-state (vec (repeat 50 {:editing false :completed true :text ""}))] 
  (def state (sa/local-storage (cell dfl-state) ::store)))

(def editing-new  (cell ""))
(def live-ones    (cell (filter (complement deleted?) state)))
(def completed    (cell (filter completed? live-ones)))
(def active       (cell (filter (complement completed?) live-ones)))

(defn loop-todos [f container]
  (loop-things state f cellvals container))

(defn clear [item]
  (assoc item :editing false :completed false :text ""))

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
