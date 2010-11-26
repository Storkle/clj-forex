(ns forex.utils 
  (:refer-clojure :exclude [=])
  (:import java.util.Calendar)
  (:require [clojure.contrib.def :as d]
	    [clojure.contrib.str-utils2 :as s]))

(defn group
  ([coll] (group coll 2))
  ([coll by] (partition-all by coll)))

(defmacro constants [& args]
  `(do ~@(map (fn [[name val]] `(def ~name ~val)) (group args 2))))
(defmacro do1 [a & body]
  `(let [ret# ~a]
     ~@body
     ret#))
(defmacro mapc [& args] `(dorun (map ~@args)))

(def is clojure.core/=)
(defmacro = [a b]
  `(swap! ~a (fn [~'% _#]
	      ~b) nil))
(defn sleep [s] (Thread/sleep s))

(defn log [e] (pr "ERROR!: " e))
(defmacro wlog [& body]
  `(try (do ~@body) (catch Exception e# (log e#))))
(defmacro mapc [& args] `(dorun (map ~@args)))
(defmacro thread [& body]
  `(let [thread# (Thread. (bound-fn [] (wlog ~@body)))]
     (.start thread#)
     thread#))


(defmacro throwf [message & args]
  (if args
    `(throw (Exception. (format ~message ~@args)))
    `(throw (Exception. ~message))))
(defmacro is? [val & message]
  `(let [result# ~val]
     (if (not result#)
       (throw (Exception. ~(or (and (first message) `(format ~@message)) (format "assert: %s" (str val)))))
       result#)))

(def split s/split)
 

(defmacro on [[& args] & body]
  (let [a (group args 2)
	first-args (map first a)
	second-args (map second a)]
    `(doseq [[~@first-args] (map vector ~@second-args)]
       ~@body)))
;;TODO: make more efficient

(defmacro constants [& args]
  `(do ~@(map #(list 'def (first %) (second %)) (group args))))

(defmacro defonce- [& args] `(d/defonce- ~@args))




(defonce *env* (atom {:timeframe 1440 :index 0})) ;default +D1+
(defn env [key] (key @*env*))
(defn env! [map]
  (= *env* (merge % map)))

;;todo: fix private!
;;todo: ignores all nils?
(defmacro wenv [[& args] & body]
  `(binding [forex.utils/*env*
	     (atom (merge @@~#'*env* (hash-map ~@args)))]
     ~@body))

