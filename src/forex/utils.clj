(ns forex.utils
  (:import java.util.Calendar)
  (:require [clojure.contrib.def :as d]
	    [clojure.contrib.str-utils2 :as s]))

(defmacro throwf [message & args]
  (if args
    `(throw (Exception. (format ~message ~@args)))
    `(throw (Exception. ~message))))
(defmacro is [val & message]
  `(let [result# ~val]
     (if (not result#)
       (throw (Exception. ~(or (first message) (format "assert: %s" (str val)))))
       result#)))

(def split s/split)

(defn group
  ([coll] (group coll 2))
  ([the-coll by]
     (loop [coll the-coll
	    result nil]
       (if (empty? coll)
	 result
	 (recur (drop by coll) (concat result (list (take by coll))))))))

(defmacro constants [& args]
  `(do ~@(map #(list 'def (first %) (second %)) (group args))))

(defmacro defonce- [& args] `(d/defonce- ~@args))




(defonce *env* (atom {:timeframe 1440 :index 0})) ;default +D1+
(defn env [key] (key @*env*))
(defn env! [map] (swap! *env* #(merge-with (fn [a b] (or b a)) % %2) map))
;;todo: fix private!
(defmacro wenv [[ & {symbol :symbol socket :socket timeframe :timeframe index :index}] & body]
  `(binding [forex.utils/*env*  (atom (merge-with #(or %2 %1) @@~#'*env* {:symbol ~symbol :socket ~socket :timeframe ~timeframe :index ~index}))] ~@body))


;;offset in minutes of server time from greg time
(def +offset+ (* 6 60))
;;TODO fix!
(defn date
  ([] (date 0))
  ([index]
     (let [cal (Calendar/getInstance)]
       (.add cal Calendar/MINUTE (+ +offset+ (* -1 (* (env :timeframe) index))))
       (.getTime cal))))



