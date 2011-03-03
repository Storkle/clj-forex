(clojure.core/use 'nstools.ns)
(ns+ forex.examples.rainbow.rainbow
     (:clone forex.default) 
     (:use forex.util.general forex.examples.rainbow.uber-trail))

(def ^{:var true} order (atom-hash))
(def ^{:var true} state (atom :monitor))

(def main (partial vma [2 2 100 1]))
(def signal (partial vma [2 2 1 1] ))
(def middle (partial vma [2 2 26 1]))

(def support (blazan-dynamic-stop [150 70 1 10000] 1))
(def resistance (blazan-dynamic-stop [150 70 1 10000] 0))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;OPENING ORDER
;;TODO: o+ with keyword - order? without protocol - mayb use multimethod instead?
(defn open-order? []
  (when-let [dir (cross? signal main)]
    (let [main-trend (wenv {:period +h1+}
			   (cond
			    (> (signal 1) (main 1)) :buy
			    (< (signal 1) (main 1)) :sell))
	  m15-rsi (wenv {:period +m15+} (rsi 10 1))]
      (cond (= dir :buy)
	    (and (> m15-rsi 50)
		 (= main-trend :buy)
		 :buy)
	    (= dir :sell)
	    (and (< m15-rsi 50)
		 (= main-trend :sell)
		 :sell)))))

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
