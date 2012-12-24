C2 TodoFRP
==========

[C2](https://github.com/lynaghk/c2/) is a data visualization library for Clojure and ClojureScript.
Under the hood, C2's `bind!` macro uses the [reflex](https://github.com/lynaghk/reflex) library to automatically setup watchers on mutable state contained within atoms.

The state of the todo list is stored in two atoms in the `todo.core` namespace; one atom for the list items and another for the current view state (all, active, or completed).
The `todo.list` namespace serves as a "view", binding the core state to the DOM and attaching event handlers.

Build
=====

Run `lein cljsbuild once` to generate the JavaScript.
Open `public/index.html` in your favorite browser.

