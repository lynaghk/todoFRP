# TodoFRP

An implementation of [TodoMVC](http://todomvc.com) using
[HLisp](http://github.com/tailrecursion/hlisp-starter/) and
[Javelin](http://github.com/tailrecursion/javelin/).

## Demo

[View the demo here](http://micha.github.com/todofrp/).

## Doit

Install [Leiningen2](https://github.com/technomancy/leiningen)

Install dependencies:

    lein deps

You can start watcher-based ClojureScript compilation:

    lein hlisp auto

HTML and JavaScript files will be created in the `resources/public` directory.
You can view the demo via file:///path/to/resources/public/index.html.

### Note

If things get weird, restarting `lein hlisp auto` usually helps.
