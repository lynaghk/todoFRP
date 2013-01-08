(ns todo.todo-controller
  (:refer-clojure :exclude [assoc!
                            map filter remove])
  (:require [todo.util :refer [assoc! update-in!
                               map filter remove]]
            [clojure.string :refer [blank?]]))

(def todomvc
  "Create a TodoMVC module within Angular.js"
  (.module js/angular "todomvc" (array)))

;;Factory fn that provides a map of fns to get/set localStorage.
;;This is just copied from the Angular demo; how useful is the factory pattern in Clojure?
(.factory todomvc "todoStorage"
          (fn []
            (let [storage-id "todos-angular-cljs"]
              (js-obj "get" #(.parse js/JSON (or (.getItem js/localStorage storage-id) "[]"))
                      "set" (fn [todos]
                              (.setItem js/localStorage storage-id (.stringify js/JSON todos)))))))

(defn TodoCtrl [$scope $location todoStorage]

  ;;Angular's $scope.$watch fn takes an expression to watch (here it's 'todos', a property of the $scope) and runs the listener fn whenever the expression's value changes.
  ;;This happens via dirty-checking rather than some kind of callback mechanism; see:
  ;;
  ;;  http://stackoverflow.com/questions/9682092/databinding-in-angularjs
  ;;  http://docs.angularjs.org/guide/concepts#runtime
  ;;
  ;;for details. Based on the callback nonsense I've run into in using my own reflex library, I'm convinced this is a superior approach.
  ;;$scope.$watch uses angular.equals for comparison; if we wanted to use cljs data structures here, we'd have to either:
  ;;1) monkeypatch angular.equals to use the cljs equality fn for efficency
  ;;2) write a macro around $scope.$watch instead of calling it directly that gives (hash x) to angular instead of x.
  (.$watch $scope "todos"
           (fn [todos]
             (.set todoStorage todos)
             (assoc! $scope
                     :doneCount (count (filter :completed todos))
                     :remainingCount (count (remove :completed todos))
                     :allChecked (every? :completed todos)))
           true)

  (.$watch $scope "location.path()"
           (fn [path]
             (assoc! $scope :statusFilter
                     (case path
                       "/active" #(not (:completed %))
                       "/completed" #(:completed %)
                       nil)))
           true)

  (when (blank? (.path $location))
    (.path $location "/"))

  (assoc! $scope
          :todos (.get todoStorage)
          :location $location
          :addTodo #(when-not (blank? (:newTodo $scope))
                      (conj! (:todos $scope) (js-obj "title" (:newTodo $scope)
                                                     "completed" false))
                      (assoc! $scope :newTodo ""))

          :editTodo (fn [todo] (assoc! $scope :editedTodo todo))

          :removeTodo (fn [todo]
                        (update-in! $scope [:todos]
                                    (fn [todos] (remove #{todo} todos))))

          :doneEditing (fn [todo]
                         (assoc! $scope :editedTodo nil)
                         (when (blank? (:title todo))
                           (.removeTodo $scope todo)))

          :clearDoneTodos (fn []
                            (update-in! $scope [:todos]
                                        (fn [todos] (remove :completed todos))))

          :markAll (fn [done]
                     (doseq [todo (:todos $scope)]
                       (assoc! todo :completed done)))))


(.controller todomvc "TodoCtrl" TodoCtrl)

;;Directive that executes an expression when the element it is applied to loses focus
(.directive todomvc "todoBlur"
            (fn []
              (fn [scope el attrs]
                (.bind el "blur"
                       #(.$apply scope (.-todoBlur attrs))))))

;;Directive that places focus on the element it is applied to when the expression it binds to evaluates to true.
(.directive todomvc "todoFocus"
            (fn [$timeout]
              (fn [scope el attrs]
                (.$watch scope (.-todoFocus attrs)
                         (fn [new-val]
                           (when new-val
                             ($timeout #(.focus (aget el 0))
                                       0 false)))))))