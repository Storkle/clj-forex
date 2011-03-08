(clojure.core/use 'nstools.ns)
(ns+ forex.examples.rainbow.rainbow
     (:clone forex.default) 
     (:use forex.util.general forex.examples.rainbow.uber-trail))

(def main (partial ema 100))
(def signal (partial vma [2 2 1 1]))
(def middle (partial vma [2 2 26 1]))

;;TODO: negative range 
(defn hh? []
  (let [[a b c d e] (map high (reverse (range 0 5)))]
    (and (< a b) (< b c) (> c d) (> d e) :sell)))
(defn ll? []
  (let [[a b c d e] (map high (reverse (range 0 5)))]
    (and (> a b) (> b c) (< c d) (< d e) :buy)))

(comment
  (defn find-spikes []
    (objects-delete-all)
    (redraw
     (def spikes
       (doall (map #(awhen (wenv {:i %} (or (hh?) (ll?)))
			   (vline (itime (+ % 2)) :color (if (= it :sell) :red :blue))
			   [% it])
		   (range 0 1000)))))))


(defn find-crosses []
    (objects-delete-all)
    (redraw
     (def crosses
       (doall (map
	       #(awhen (wenv {:i %} (open-order?))
		       (vline (itime %) :color (if (= it :sell) :red :blue))
		       [% it])
	       (range 0 1000))))
     (vline (itime 999))))
;;TODO: weird, update service was down, nothing updating!!!
(defn open-order? []
  (cross? signal main))
;;TODO: itime should also shift!
(defn dir [period]
    (let [i (ibarshift (itime 1) period)]
      (wenv {:period period :i 0} (or (and (> (cci 30 i) 0) (> (rsi 14 i) 50) :buy) (and (< (cci 30 i) 0) (< (rsi 14 i) 50) :sell)))
    
      ))

;;TODO: receive-indicator! for ibarshift and itime!
;;TODO: requested data in updating state error! argh! i thought we had fixed that...
(defn open-order? []
  (awhen (cross? signal main)
	 (or
	  (and (= it :buy)
	       (> (cci 30 1) 0)
	       (> (rsi 14 1) 50)
	       (= (dir +d1+) :buy)
	       :buy)
	  (and (= it :sell)
	       (< (cci 30 1) 0)
	       (< (rsi 14 1) 50)
	       (= (dir +d1+) :sell)
	       :sell))))

;;GBPJPY = +h4+
;;EURUSD = +d1+
;;USDCHF = 3
