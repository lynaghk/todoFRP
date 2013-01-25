# Flapjax with JavaScript • [TodoFRP](https://github.com/lynaghk/todoFRP)

This TodoFRP implementation drives
[Flapjax](http://www.flapjax-lang.org/) with plain JavaScript.

You can check out a running demo over at [http://alandipert.github.com/todoFRP/flapjax-js/](http://alandipert.github.com/todoFRP/flapjax-js/).

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
collection.  I avoided dynamic behaviors because I think they would
add more complexity than they'd remove.

## Routing

An external routing library is not used.  Instead, the current route
is modeled as a Behavior.  The "route table" is a mapping of possible
values of this behavior to collection filter predicates.  Whenever the
route behavior changes, the filter predicate does, thus changing which
todos are rendered.

## License

Copyright (C) 2013 [Alan Dipert](http://alan.dipert.org/)

Distributed under the Eclipse Public License, the same as Clojure.
