(ns todo.main
  (:require [todo.core :as core]
            [todo.ui :as ui]
            [jayq.core :as jq]
            [widje.core :as widje]))

(defn main
  "Init function to run on page load."
  []
  (core/load-todos!)
  (core/update-filter!)
  (ui/render))

(jq/document-ready main)
