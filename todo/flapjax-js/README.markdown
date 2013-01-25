# Flapjax with JavaScript • [TodoFRP](https://github.com/lynaghk/todoFRP)

This TodoFRP implementation drives
[Flapjax](http://www.flapjax-lang.org/) with plain JavaScript.

## Storage

Todos are persisted in localStorage as an array of `{id: <uuid>,
title: <string>, completed: <boolean>}` stringified objects.  This
array is represented by a `LocalStorage` object that maintains a
Flapjax Behavior, and internally provides an interface similar to a
Clojure atom, with `deref` and `swap` methods.

Modification of local storage causes the value of this Behavior to
vary, and drives the rendering of todos to the DOM.

Reactive remote storage would be trivially implementable by creating a
Storage object backed by a web service instead of localStorage.

## Rendering and Mutation

All todos are rendered every time there is a modification to the
LocalStorage object.  Every rendered todo manages its own mutation
using an approach modeled after the [Flapjax Drag and Drop
Demo](http://www.flapjax-lang.org/demos/index.html#drag).  There are
no mutable identities - only EventStreams and Behaviors.

There are also no dynamically created Behaviors, and as a result,
state change is fairly coarse-grained - it occurs at the level of the
collection.  Finer-grained, dynamic behaviors may make the
implementation more performant at the expense of increased complexity.
Dynamic behaviors may also introduce memory leaks or other performance
problems.

## Routing

An external routing library is not used.  Instead, the current route
is modeled as a Behavior.  The "route table" is a mapping of possible
values of this behavior to collection filter predicates.  Whenever the
route behavior changes, the filter predicate does, thus changing which
todos are rendered.

## License

Copyright (C) 2013 [Alan Dipert](http://alan.dipert.org/)

Distributed under the Eclipse Public License, the same as Clojure.
