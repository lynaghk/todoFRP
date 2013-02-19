
(defproject
  todomvc-hlisp "0.1.0-SNAPSHOT"
  :description  "TodoMVC using hlisp."
  :license      {:name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :plugins      [[lein-hlisp "1.0.0"]]
  :dependencies [[hlisp-macros "1.0.0"]
                 [hlisp-util "0.1.0-SNAPSHOT"]
                 [hlisp-reactive "1.0.0-SNAPSHOT"]
                 [alandipert/storage-atom "1.1.1"]]
  :eval-in      :leiningen
  :hlisp        {:html-src    "src/html"
                 :cljs-src    "src/cljs"
                 :html-out    "resources/public"
                 :base-dir    nil
                 :pre-script  "./script/pre-compile"
                 :post-script "./script/post-compile"
                 :cljsc-opts  {:pretty-print   true
                               :optimizations  :whitespace}})
