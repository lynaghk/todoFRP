;;Namespace that extends JavaScript's object and array to play nicely
;;with Clojure's semantics and act as transient collections.

(ns todo.util
  (:require [goog.object :as gobject])
  (:refer-clojure :exclude [assoc!
                            map filter remove]))

(defn p [x]
  (.log js/console x)
  x)

(defn pp [x]
  (.log js/console
        (cond
         (goog.isArray x) x
         :else (pr-str x)))
  x)


;;TODO: Why isn't this in cljs core?
(defn assoc!
  "Transient associate allowing multiple k/v pairs."
  ([tcoll k v]
     (-assoc! tcoll k v))
  ([tcoll k v & kvs]
     (let [ret (assoc! tcoll k v)]
       (if kvs
         (recur ret (first kvs) (second kvs) (nnext kvs))
         ret))))

(defn update-in!
  ([m [k & ks] f & args]
     (if ks
       (assoc! m k (apply update-in! (get m k) ks f args))
       (assoc! m k (apply f (get m k) args)))))

;;To use Clojure's idiomatic seq-manipulation functions (filter, remove, &c.) on JavaScript arrays and objects we need to lift cljs.core's functions into ones that can delegate into JavaScript or ClojureScript appropriate implementations.
(defn seqtype [x]
  (cond
   (goog.isArray x)  :js-arr
   (seq? x)          :seq
   (goog.isObject x) :js-obj))


(defmulti filter (fn [pred coll] (seqtype coll)))

(defmethod filter :js-arr
  [pred a]
  (.filter a #(pred %)))

(defmethod filter :seq
  [pred coll]
  (cljs.core.filter pred coll))


(defmulti remove (fn [pred coll] (seqtype coll)))

(defmethod remove :js-arr
  [pred a]
  (.filter a #(not (pred %))))

(defmethod remove :seq
  [pred coll]
  (cljs.core.remove pred coll))


(defmulti map (fn [pred coll] (seqtype coll)))

(defmethod map :js-arr
  [f a]
  (.map a f))

(defmethod map :seq
  [f coll]
  (cljs.core.map f coll))




;;Make JavaScript objects and arrays place nicely with ClojureScript by implementing lookup protocols and acting like Clojure's transient collections

(defn strkey [x]
  (if (keyword? x)
    (name x)
    x))

(extend-type object
  ILookup
  (-lookup
    ([o k]
       (aget o (strkey k)))
    ([o k not-found]
       (if-let [res (aget o (strkey k))]
         res not-found)))

  IEmptyableCollection
  (-empty [_]
    (js-obj))

  ITransientCollection
  (-conj! [o [k v]]
    (assoc! o k v))
  (-persistent! [_]
    (throw (js/Error. "JavaScript object isn't a real transient, don't try to make it persistent.")))

  ITransientAssociative
  (-assoc! [o k v]
    (aset o (strkey k) v)
    o)

  ITransientMap
  (-dissoc! [o key]
    (gobject/remove o key)
    o))

(extend-type array
  IEmptyableCollection
  (-empty [a]
    (array))

  ITransientCollection
  (-conj! [a x]
    (.push a x)
    a)
  (-persistent! [_]
    (throw (js/Error. "JavaScript array isn't a real transient, don't try to make it persistent.")))

  ITransientAssociative
  (-assoc! [a k v]
    (aset a (strkey k) v)
    a))