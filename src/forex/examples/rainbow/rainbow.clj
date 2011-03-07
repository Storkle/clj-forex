(clojure.core/use 'nstools.ns)
(ns+ forex.examples.rainbow.rainbow
     (:clone forex.default) 
     (:use forex.util.general forex.examples.rainbow.uber-trail))

(def ^{:var true} order (atom-hash))
(def ^{:var true} state (atom :monitor))

(def main (partial vma [2 2 100 1]))
(def signal (partial vma [2 2 1 1] ))
(def middle (partial vma [2 2 26 1]))

(def support (partial blazan-dynamic-stop [150 70 1 10000] 1))
(def resistance (partial blazan-dynamic-stop [150 70 1 10000] 0))


(def crosses (doall (map
	       #(awhen (wenv {:i %} (open-order?))
		       (vline (itime %) :color (if (= it :sell) :red :blue))) (range 0 1000))))
(defn dir [period]
  (wenv {:period period}
	(cond
	 (> (signal 1) (main 1)) :buy
	 (< (signal 1) (main 1)) :sell)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;OPENING ORDER
;;TODO: o+ with keyword - order? without protocol - mayb use multimethod instead?
(defn open-order? []
  (cross? signal main))


;;max 20 pips, min 5, or then blazan support
(defn open-order [dir]
  (let [sl (if (= dir :buy)
	     (let [r (resistance 1)
		   d1 (+ (close 1) (pip 5))
		   d2 (+ (close 1) (pip 20))]
	       (cond
		(< r d1) d1
		(> r d2) d2
		true r))
	     (let [r (support 1)
		   d1 (- (close 1) (pip 5))
		   d2 (- (close 1) (pip 20))]
	       (cond
		(> r d1) d1
		(< r d2) d2
		true r)))
	lots (risk 1 sl (close))]
    (order! {:lots lots :state :init :type dir})))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;main
(defn monitor []
  (when-let [it (open-order?)]
    (awhen (open-order it)
	   (reset! order it)
	   (reset! state :manage)
	   (out "opened order ... monitoring"))))


(defn gobble []
  (when (uber-trail! order)
    (reset! order nil)
    (reset! state :monitor)))
 
(defn start [& args]
  (wenv {:period +m5+}
	(cond (= @state :monitor) (monitor)
	      (= @state :manage) (gobble))))
