# TodoFRP

An inplementation of [TodoMVC](http://todomvc.com) using
[HLisp](http://github.com/tailrecursion/hlisp-starter/) and
[Javelin](http://github.com/tailrecursion/javelin/).

## Demo

[View the demo here](http://micha.github.com/todofrp/).

## Overview

Frontend development: there are many ways to skin this cat, and so far none
of them seem to really provide the model we need. HLisp gives us a new model
as a foundation for experimenting with different ways of building the frontend.

For this application I wrote the
[hlisp-reactive](http://github.com/micha/hlisp-reactive/) library to tie
HLisp and Javelin together. This library provides a macro that gives the
designer the ability to attach reactive behaviors to elements in the
markup, similar to [angularjs](http://angularjs.org). Javelin provides the
FRP (Functional Reactive Programming) underpinnings.

## Workflow

There are two files of interest: `src/html/index.html` and
`src/include/index.cljs`. Those are combined in the compiler to create the
final `index.html` and `main.js` files that comprise the application. It is
assumed that there will be a "programmer" and a "designer" working on the
application.

* The markup is all in the `index.html` file. There is no ClojureScript code
  creating elements or anything like that. The designer can change any element
  in the final page by editing `src/html/index.html`. The programmer doesn't
  ever need to touch this file.
  
* The FRP code that sets up the application state is all in the
  `src/include/index.cljs` file, which is spliced into the markup by the HLisp
  compiler via the `<include>` tag. The designer never needs to mess around in
  this file.

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
to DOM elements, thereby conveying this new output to the user.

This suggests a very simple model for DOM interactions:

* Input is collected via event listeners on the DOM elements. The event
  handlers do nothing more than process the event and pass information to
  the underlying FRP state machine.
* Output is the result of FRP propagation from the state machine after a
  change in the application's state.

With this model in place, the more general properties are as follows:

* DOM elements are not created nor destroyed in the running of the application.
* Connections between the DOM and the FRP state machine are
  [delegated](http://api.jquery.com/delegate/) and indirect.
* There are no direct connections between elements. Usually such connections
  are completely unnecessary. However, when elements must communicate they may
  do so via the state machine, as I/O operations. There are examples of both
  in this todo application.
  
This model simplifies DOM interactions considerably. In fact, this model can
maybe be boiled down to a single principle: all state is contained in the
underlying FRP machinery, and never in the DOM&mdash;the DOM serves only to
collect input from the user and convey to them output from the state machine.

### Spreadsheets

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
