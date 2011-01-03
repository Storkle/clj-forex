;;forex.module.error.common - utilities for handling mql errors
 
(ns forex.module.error.common
  (:require [forex.backend.mql.socket-service :as s])
  (:use utils.general emacs 
	forex.util.general))
 
;;raw receive
(defn raw-receive [msg]
  (s/receive msg))
(defn raw-receive-lst [msg]
  (split (raw-receive msg) #" +"))


;;receive with errors
(defn receive! [msg]
  (let [spl (raw-receive-lst msg)]
    (if (= (first spl) "error")
      (throwf "MQL error %s" (second spl))
      (join " "  spl))))

(defn receive-lst! [msg]
  (let [spl (raw-receive-lst msg)]
    (if (= (first spl) "error")
      (throwf "MQL error %s" (second spl))
      spl)))

(defn receive-double! [msg]
  (Double/parseDouble (receive! msg)))

;;receive with default instead of errors, returns error object for errors
;;is customizable to default to errors!
(defrecord MqlErr [e])
(defonce- *er* (gensym)) 
(def *default* *er*)

(defn e? [a] (instance? MqlErr a))

(defmacro iff-let
  ([test then] `(iff-let ~test ~then nil))
  ([[var test] then else]
     `(let [~var ~test]
	(if (and test (not (e? ~var)))
	  ~then
	  ~else))))

(defmacro iff
  ([test then] `(iff ~test ~then nil))
  ([test then else]
     `(iff-let [~'it ~test]
	       ~then ~else)))
 
(defn receive
  ([msg] (receive msg *default*))
  ([msg default]
     (let [spl (raw-receive-lst msg)]
       (if (= (first spl) "error")
	 (if (= default *er*)
	   (MqlErr. (Integer/parseInt (second spl)))
	   (if (fn? default) (default (MqlErr. (Integer/parseInt (second spl)))) default))
	 (join "" spl)))))

(defn receive-double
  ([msg] (receive-double msg *default*))
  ([msg default]
     (let [spl (raw-receive-lst msg)]
       (if (= (first spl) "error")
	 (if (= default *er*)
	   (MqlErr. (Integer/parseInt (second spl)))
	   (if (fn? default) (default (MqlErr. (Integer/parseInt (second spl)))) default))
	 (Double/parseDouble (join " "  spl))))))
  
(defn receive-int [s]
  (iff (receive-double s)
       (int it)
       it))
