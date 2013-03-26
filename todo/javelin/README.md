# Javelin [TodoFRP](https://github.com/lynaghk/todoFRP)

An implementation of ToDoFRP using [Javelin](https://github.com/tailrecursion/javelin), 
[Domina](https://github.com/levand/domina) and [Dommy](https://github.com/Prismatic/dommy)

## Demo

[View the demo here.](http://priornix.github.com/todoFRP/todo/javelin/pubic/index.html)

## Build

Install [Leiningen2](https://github.com/technomancy/leiningen)

Run `lein cljsbuild once` to generate the JavaScript.

Open `public/index.html` in your favorite browser.

## Reactive programming

Javelin drives the functional reactive programming of this project.

The full todo list is stored in the `todo` cell and other cells then react
to changes to this cell value. When the `todo` cell value changes: 

* The todo list is rendered in `show-todos`
* `save-todos!` is called to persist the todo list to localStorage
* The values of the `active-items` and `completed-items`
  cells are changed, which in turn updates the active and completed
  display count.

## Routing

Routing is handled by the Google Closure library. When the route
changes, the content of the `filter-fn` cell is changed. This causes the 
`show-todos` cell to react to this change, in turn rendering which 
todos are to be displayed.

## Domina, Dommy

Domina is used to select and manipulate the DOM elements, and for
event handling. Dommy is only used for HTML templating during the
display of the todos. These libraries can be replaced with other
equivalent libraries as required.

## License

Copyright © 2013 [Michael Lim](http://github.com/priornix)

Distributed under the Eclipse Public License, the same as Clojure.
