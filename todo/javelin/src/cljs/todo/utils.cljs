(ns todo.utils
  (:require [clojure.browser.event :as event]
            [goog.History :as history]
            [goog.history.Html5History :as history5])
  (:use [domina :only [add-class! remove-class!]]))

(defn toggle-class! [node class pred]
  (if pred
    (add-class! node class)
    (remove-class! node class)))

(extend-type goog.History
  event/EventType
  (event-types [this]
    (into {}
          (map
           (fn [[k v]]
             [(keyword (. k (toLowerCase)))
              v])
           (js->clj goog.history.EventType)))))

(defn history
  [callback]
  (let [h (if (history5/isSupported)
            (goog.history.Html5History.)
            (goog.History.))]
    (do (event/listen h "navigate"
                      (fn [e]
                        (callback {:token (keyword (.-token e))
                                   :type (.-type e)
                                   :navigation? (.-isNavigation e)})))
        (.setEnabled h true)
        h)))

