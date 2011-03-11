(ns forex.module.indicator.util
  (:use forex.util.core
	[clj-time.core :exclude [extend start]]
	clj-time.coerce))
(defn now-seconds [] (.intValue (/ (to-long (now)) 1000M)))

(defn env? [{:keys [symbol period]}]
  (and (string? symbol) (integer? period)))
(defn mklst [a]
  (if (clojure.contrib.core/seqable? a) a (list a)))
(defn >=? [a b] (and (number? a) (number? b) (>= a b)))

(defn env-dispatch
  ([arg] (env-dispatch arg false))
  ([[a b :as args] assert]
     (let [e (merge
	      @*env*
	      (if (map? a)
		a
		(let [new-env
		      (cond
		       (and (integer? a) (string? b))
		       {:period a :symbol b}
		       (and (string? a) (integer? b))
		       {:period b :symbol a}
		       (integer? a) {:period a}
		       (string? a) {:symbol a}
		       true {})] 
		  (if (map? b) (merge b new-env) new-env))))]
       (if assert (do (env? e) e) e))))  

(defn set-arglists! [to from]
  (alter-meta! to merge {:arglists (:arglists (meta from))}))


(defn subv
  ([v start default]
     (subv v start (count v) default))
  ([v start end default]
     (try (subvec v start end) (catch IndexOutOfBoundsException e default))))
 