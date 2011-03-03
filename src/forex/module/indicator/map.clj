(clojure.core/use 'nstools.ns)
(ns+ forex.module.indicator.map
     (:clone clj.core)
     (:use forex.module.indicator.util)
     (:use forex.util.emacs
	   [clj-time.core :exclude [extend start]]
	   [clj-time.coerce]
	   forex.util.spawn
	   forex.util.core
	   forex.util.log
	   forex.util.general
	   forex.module.account.utils 
	   forex.module.error)   
     (:require clojure.contrib.core
      [forex.backend.mql.socket-service :as backend])
     (:require [forex.module.error :as s])) 
  
(def *max* 1000)
(def *now* (to-long (now)))
(defonce *indicators* (atom {}))
 
(defn indicator-protocol
  "retrieve protocol string needed for indicator"
  [{:keys [name param mode symbol
	   period from max to min]
    :or {max *max*
	 mode 0
	 from *now* to 0 min 1
	 symbol (env :symbol)
	 period (env :period)}}] 
  {:pre [(string? name)
	 (env? {:symbol symbol :period period})
	 (>=? max 0)
	 (>=? mode 0)
	 (>=? from 0)
	 (>=? to 0)
	 (>=? max min)]}
  (format "%s %s %s %s %s %s %s %s %s"
	  name symbol  period  mode from to min max
	  (apply str (interpose " " (mklst param)))))

(defn indicator-vector
  "retrieve indicator vector from socket service"
  [{:keys [max] :or {max *max*} :as this}
   &
   [{:keys [retries] :or {retries 3}}
    proto]]
  {:pre [(or (nil? proto) (string? proto))]}
  (if (= max 0)
    []
    (receive-indicator (or proto (indicator-protocol this)) retries)))     

(defn clear []
  (var-root-set #'*indicators* (atom {})))  

;;TODO: handle situation where multiple pids get it?? - not too important, actually
(defn add-pid [this return]
  (swap! *indicators*
	 merge {(:id this)
		(merge this {:pid (conj (:pid this)
					(self))})})
  return) 

(defn subv
  ([v start default]
     (subv v start (count v) default))
  ([v start end default]
     (try (subvec v start end) (catch IndexOutOfBoundsException e default))))

(defn indicator-vector-memoize
  "retrieve indicator vector from storage or from socket service if does not exist.
   Add current pid to indicator also. Returns indicator vector"
  ([args] (indicator-vector-memoize args @*env*))
  ([{:keys [name param mode] :or {mode 0 param nil}} e]
     (let [{:keys [symbol period] :as e} e
	   id [symbol period name param mode]
	   val (get @*indicators* id)]
       (aif val
	    (add-pid it (subv (:vec it) (or (:i e) 0) []))
	    (let [indicator
		  {:name name :id id :pid #{(self)} 
		   :param param :mode mode :symbol symbol
		   :period period
		   :max *max* :to 0}
		  ret (indicator-vector indicator)]
	      (aif ret
		   (do
		     (swap! *indicators* assoc id
			    (merge indicator {:vec ret}))
		     ret)
		   (throwf it)))))))      
  
(defn indicator-vector1-memoize [index & args]
  (nth (apply indicator-vector-memoize args) index 0))
 
