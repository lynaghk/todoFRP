
(defproject
  todomvc-hlisp "0.1.0-SNAPSHOT"
  :description  "TodoMVC using hlisp."
  :license      {:name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :plugins      [[tailrecursion/lein-hlisp "2.0.3-SNAPSHOT"]]
  :dependencies [[tailrecursion/hlisp-macros "1.0.0"]
                 [tailrecursion/hlisp-util "0.1.0-SNAPSHOT"]
                 [tailrecursion/hlisp-reactive "1.0.2-SNAPSHOT"]
                 [alandipert/storage-atom "1.1.1"]]
  :eval-in      :leiningen
  :hlisp        {:html-src    "src/html"
                 :html-out    "resources/public"
                 :cljsc-opts  {:pretty-print   true
                               :optimizations  :whitespace}})
