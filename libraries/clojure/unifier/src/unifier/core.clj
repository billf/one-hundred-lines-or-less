(ns unifier.core
  (:import (clojure.lang Sequential))
  (:require [clojure.walk]))

(defn variable? [x]
  (symbol? x))

(defn add-var [env var val]
  (assoc env var val))

(defn lookup [env var]
  (if-let [val (get env var)]
    (if (variable? val)
      (lookup env val)
      val)
    var))

(defn failed? [x]
  (= x :unify/failed))

(def unify)
(def unify-terms)

(defn unify [env x y]
  (if (map? env)
    (let [x-val (lookup env x)
          y-val (lookup env y)]
      (cond 
        (variable? x-val) (add-var env x-val y-val)
        (variable? y-val) (add-var env y-val x-val)
        :else (unify-terms env x-val y-val)))
    env))

(defmulti unify-terms
  (fn [env x y] [(type x) (type y)]))

(defn reducer [env [x y]]
  (unify env x y))

(defmethod unify-terms [Sequential Sequential]
  [env x y]
  (if (= (count x) (count y))
    (reduce reducer env (map vector x y))
    :unify/failed))

(defmethod unify-terms :default [env x y]
  (if (= x y) 
    env
    :unify/failed))

(defn substitute [[env expr]]
  (clojure.walk/postwalk (fn [x] (lookup env x)) expr))




(comment

  (-> {}
      (unify 'x 2)
      (unify 'y 'x)
      (unify 'z 'y)
      (lookup 'z))

  (-> {}
      (unify 'x 'y)
      (unify 'y 'z)
      (unify 'z 2)
      (lookup 'x))


  (-> {}
      (unify 2 'x)
      (unify 'x 'y)
      (unify 'z 'y)
      (lookup 'z))


  (substitute '[{x 3} [x [x [x [x [x [[x]]]]]]]]))
