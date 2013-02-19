# TodoFRP

An inplementation of [TodoMVC](http://todomvc.com) using
[HLisp](http://github.com/tailrecursion/hlisp-starter/) and
[Javelin](http://github.com/tailrecursion/javelin/).

## Demo

[View the demo here](http://micha.github.com/todofrp/).

## Overview

Frontend development: there are many ways to skin this cat, and so far none
of them seem to really provide the model we need. HLisp gives us a new model
as a foundation for investigating new ways to build the frontend.

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
  (User Interface) and does the actual computing.

* The programmer exposes and documents all FRP cells and state-mutating
  functions, and delivers the application to the designer.

* The designer codes the markup and styling as they see fit, using simple
  reactive behavior attributes to wire up the UI elements to the state machine.

* Unit tests can be written to fully exercise the FRP part separately, while
  still encompassing all operations required by the application specs.

* Selenium or PhantomJS etc. can be used to exercise the UI separately from
  the FRP part.

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
