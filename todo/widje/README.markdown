Widje TodoFRP
==========

[Widje](https://github.com/Flamefork/widje) is a simple implementation of widget concept
on top of [crate](https://github.com/ibdknox/crate) and [jayq](https://github.com/ibdknox/jayq).
`crate` provides templating and atom->dom binding while `widje` makes
dom events and other supporting logic easy to handle (plus some other useful stuff).

Widget is a function that returns fully prepared DOM node, with all bindings, events and data already bound.
Using this, UI is a composition of widgets, similar to program is a composition of functions.

- `todo.todos`: todo list itself (stored in `/!list` atom) and "model" operations on this list.
- `todo.core`: UI state and operations.
- `todo.widgets`: UI widgets that use data from `core` and triggers operations from `core` and `todos`.

Build
=====

Run `lein cljsbuild once` to generate the JavaScript.
Open `public/index.html` in your favorite browser.

