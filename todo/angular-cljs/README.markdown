Angular.js + ClojureScript TodoFRP
==================================

The purists might not like this one, this implementation is an unholy union of Google's [Angular.js](http://angularjs.org) JavaScript framework with the application code written in ClojureScript.
In particular, state is stored within plain JavaScript data structures, not ClojureScript's immutable data structures.
ClojureScript's are semantically much nicer, but have two major downsides:

1. Performance, especially w.r.t. (de)serialization (not surprising, since `JSON.parse` and `JSON.generate` are provided by the browser, whereas ClojureScript's are implemented in JavaScript)
2. Angular.js provides two-way data binding: e.g., a DOM checkbox can be directly tied to a boolean field of a JavaScript object such that changing the field programmatically updates the checkbox, and interacting with the checkbox changes the object's field. It's not clear where or how ClojureScrpt's immutable data structures would fit into this picture.

This example extends JavaScript's array and object types to act like Clojure's transients and can be mutated in place with things like `assoc!`.
ClojureScript's `map`, `filter`, and `remove` seq manipulation functions are replaced by implementations that can act both on seqs and JavaScript types.
All of these modifications are in `util.cljs`, with `todo_controller.cljs` containing the application code.



More thoughts on Angular.js + ClojureScript here: https://gist.github.com/3856153

Build
=====

Run `lein cljsbuild once` to generate the JavaScript.
Open `public/index.html` in your favorite browser.
