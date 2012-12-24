(ns todo.main
  (:use [c2.event :only [on-load]])
  (:require [todo.core :as core]))


(defn main
  "Init function to run on page load."
  []
  (core/load-todos!)
  (core/update-filter!))

(on-load main)
