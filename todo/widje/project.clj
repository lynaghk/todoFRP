(defproject todo "0.1.0"
  :description "TodoFRP with Widje"
  :dependencies [[widje "0.1.6"]
                 [jayq "2.0.0"]
                 [crate "0.2.3"]]
  
  :min-lein-version "2.0.0"
  :source-paths ["src/cljs"]

  :plugins [[lein-cljsbuild "0.2.10"]]

  :cljsbuild {:builds
              [{:source-path "src/cljs"
                :compiler {:output-to "public/todo.js"
                           :pretty-print true
                           :optimizations :whitespace}}]})
