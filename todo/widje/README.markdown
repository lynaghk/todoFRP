Widje TodoFRP
==========

Work in progress

todo

The state of the todo list is stored in two atoms in the `todo.core` namespace; one atom for the list items and another for the current view state (all, active, or completed).
The `todo.list` namespace serves as a "view", binding the core state to the DOM and attaching event handlers.

Build
=====

Run `lein cljsbuild once` to generate the JavaScript.
Open `public/index.html` in your favorite browser.

