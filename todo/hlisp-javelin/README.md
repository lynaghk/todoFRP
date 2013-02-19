# TodoFRP

An inplementation of [TodoMVC](http://todomvc.com) using
[HLisp](http://github.com/tailrecursion/hlisp-starter/) and
[Javelin](http://github.com/tailrecursion/javelin/).

## Demo

[View the demo here](http://micha.github.com/todofrp/).

## Overview

The primary problems of frontend application development are:

* The data-level DOM composes poorly with associated program-level
  application logic.  Markup is data, not meaning.  Programs are
  both.
* The complexity imposed by JavaScript's asynchronous semantics
  combined with the DOM event model.

The combination of HLisp and Javelin used in this project represents
an attempt to solve both problems, respectively:

* HLisp lifts markup to the function-level by compiling HTML as
  ClojureScript source code, and by providing semantics that make the
  resulting program composable with application logic at a unified
  and wholly programmatic level.
* Javelin manages mutation and event collection, and provides
  semantics for maintaining application state using a model inspired
  by spreadsheets and techniques inspired by FRP.

The [hlisp-reactive](http://github.com/micha/hlisp-reactive/) library is an
attempt to tie HLisp and Javelin together. It provides a macro that gives
the designer the ability to attach reactive behaviors to elements in the
markup, similar to [angularjs](http://angularjs.org). Javelin provides
the FRP (Functional Reactive Programming) underpinnings.

## Workflow

There are two files of interest:
[src/html/index.html](src/html/index.html) and
[src/include/index.cljs](src/include/index.cljs). Those are combined
in the HLisp compiler to create the final `index.html` and `main.js`
files that comprise the application. It is assumed that there will be
a "programmer" and a "designer" working on the application.

* The markup is all in the [index.html](src/html/index.html)
  file. There is no ClojureScript application code creating elements
  or otherwise manipulating the DOM outside of this file. The designer
  may definitively modify any element by editing
  [index.html](src/html/index.html). The programmer doesn't ever need
  to touch this file.
* The FRP code that sets up the application state is all in the
  [src/include/index.cljs](src/include/index.cljs) file, which is
  spliced into the markup by the HLisp compiler via the `<include>`
  tag. The designer never needs to mess around in this file.
* The programmer writes the FRP code in ClojureScript. This code encompasses
  the complete logical state machine that is the operational specification of
  the application. That is to say, the machinery that runs underneath the UI
  (User Interface) and does the actual computing. It is worth noting that
  the machinery here does not include any templates, DOM elements, DOM-as-data
  ([hiccup](http://github.com/weavejester/hiccup), for example) or any other
  reference to the DOM in any way.
* The programmer exposes and documents all FRP cells and state-mutating
  functions, and delivers the application to the designer. The programmer may
  even provide a simple wireframe as a starting point for the designer, if
  they like.
* The designer codes the markup and styling as they see fit, using simple
  reactive behavior attributes to wire up the UI elements to the state machine.
  It is important to note that the designer does no programming here&mdash;all
  that is required is to declaritively wire individual, isolated elements in
  the markup to the exposed state machine cells and functions.
* Unit tests can be written to fully exercise the FRP part separately, while
  still encompassing all operations required by the application specs.
* Selenium or PhantomJS etc. can be used to exercise the UI separately from
  the FRP part.

## DOM Elements

In an HLisp+Javelin application like this one, DOM elements have only
one role: they are an I/O (Input/Output) mechanism. When the elements are
first inserted into the page they are conveying output to the user. The
user may then interact with these elements (clicking on them, entering
text into input boxes, etc.).  These interactions result in events which
are the input passed to the underlying FRP state machinery. Changes to
the FRP state may then propagate via reactive behaviors to cause changes
to DOM elements, thereby conveying new output to the user.

This suggests a very simple model for DOM interactions:

* Input is collected via event listeners on the DOM elements. The event
  handlers do nothing more than process the event and pass information to
  the underlying FRP state machine.
* Output is the result of FRP propagation from the state machine after a
  change in the application's state and is conveyed to the user by updating
  properties of the DOM elements (id, class, css properties, etc.).

With this model in place, the more general properties are as follows:

* DOM elements are neither created nor destroyed in the running of the
  application.
* Behaviors are set up once, when the page is first constructed, and remain in
  place immutably. No behaviors are attached or removed while the program runs.
* Connections between the DOM and the FRP state machine are
  [delegated](http://api.jquery.com/delegate/) and indirect.
* There are no direct connections between elements. When elements must
  communicate they do so via the state machine as I/O operations.
  
This model simplifies DOM interactions considerably. In fact, this model can
maybe be boiled down to a single principle: all state is contained in the
underlying FRP machinery, and never in the DOM&mdash;the DOM serves only to
collect input from the user and convey to them output from the state machine.

## Spreadsheets

The connection between FRP-backed web applications and spreadsheets is
interesting. Spreadsheets are perhaps the most accessible and successful
programming model we have today. Imagine a complex spreadsheet, coded by
a skilled programmer. While the average user may not fully understand the
formulas or how the spreadsheet really works, they can still make good use
of the thing by simply changing the values of cells. Even more interesting
is that this user can create all sorts of charts and visualizations of
the data in the spreadsheet; reactive, interactive graphs and forms.

This is pretty amazing. Perhaps frontend development can achieve some of
the same success. The UI can perhaps be written as the "charts" that react
to the underlying spreadsheet-like FRP state machine?

## Development

Install [Leiningen2](https://github.com/technomancy/leiningen)

Install dependencies:

    lein deps

You can start watcher-based ClojureScript compilation:

    script/autobuild

HTML and JavaScript files will be created in the `resources/public` directory.

### Note

When compiling the application you may come across some errors similar to this
one:

    line 1 column 807 - Error: <section> is not recognized!

It's okay. This is just the HTML-tidy library failing to pretty-print some
optional HTML (HTML-tidy doesn't know how to deal with HTML5 elements). It's
nothing to worry about.

If things get weird, deleting the `hlwork` directory and restarting the 
compiler usually helps. (This is where the HLisp compiler creates its work
tree.)
