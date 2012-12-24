(defproject todo-angular "0.1.0"
  :description "Todo list."
  
  :min-lein-version "2.0.0"
  :source-paths ["src/cljs"]

  :plugins [[lein-cljsbuild "0.2.10"]]

  :cljsbuild {:builds
              [{:source-path "src/cljs"
                :compiler {:output-to "public/todo.js"
                           :pretty-print false
                           ;;Can't use advanced compilation with angular.js yet
                           :optimizations :whitespace}}]})
