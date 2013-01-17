Widje TodoFRP
==========

Widje is a simple implementation of widget concept on top of `crate` and `jayq`.
`crate` provides atom->dom binding while `widje` makes dom events easy to handle (plus some other useful stuff).


The state of the todo list is stored in two atoms in the `todo.core` namespace; one atom `!todos` for the list items and another `!filter` for the current view state (all, active, or completed).
The `todo.ui` namespace serves as a "view", binding the core state to the DOM and attaching event handlers.
It has few additional view-only atoms to clarify view concepts: `!visible-todos`, `!editing`, `!stats`.

Build
=====

Run `lein cljsbuild once` to generate the JavaScript.
Open `public/index.html` in your favorite browser.

