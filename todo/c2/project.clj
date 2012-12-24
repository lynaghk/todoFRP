(defproject todo "0.1.0"
  :description "TodoFRP with C2"
  :dependencies [[com.keminglabs/c2 "0.2.1"]]
  
  :min-lein-version "2.0.0"
  :source-paths ["src/cljs"]

  :plugins [[lein-cljsbuild "0.2.10"]]

  :cljsbuild {:builds
              [{:source-path "src/cljs"
                :compiler {:output-to "public/todo.js"
                           :pretty-print false
                           :optimizations :advanced}}]})
