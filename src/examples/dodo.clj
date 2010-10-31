(ns examples.dodo (:use forex.binding forex.utils))

(defn take-by [n coll]
  (lazy-seq				
    (when-let [s (and (>= (count coll) n) (seq coll))]
      (cons (take n s) (take-by n (rest s))))))

(defn trader-stop [order]
  (if (open? order)
    (if (below (price) green)inter
      (close-order order)
					;every 10 seconds, udpate stoploss 
      (if (zero? (% i 10)) (modify order (green) 0)))))

(defn trade! [direction price sl]
  (let [lots (min 10 (/ (* .03 (free-margin)) (dollars (abs (- price sl)))))]
    (trader-stop! (market-order {:direction direction :price price :lots lots :sl sl}))))

(defn peak-type [item]
  (let [[a b c m d e f] item]
    (cond
      (and (< a b) (< b c) (< c m) (< d m) (< e d) (< f e)) :peak ;3x3
      (and (< b c) (< c m) (< d m) (< e d) (< f e)) :peak	  ;2x3
      (and (< a b) (< b c) (< c m) (< d m) (< e d)) :peak	  ;3x2      
      (and (> a b) (> b c) (> c m) (> d m) (> e d) (> f e)) :valley ;3x3
      (and (> a b) (> b c) (> c m) (> d m) (> e d) ) :valley ;3x2
      (and (> b c) (> c m) (> d m) (> e d) (> f e)) :valley ;2x3
      true nil)))
(defn middle [i] (/ (+ (close i) (open i)) 2.0))
(defn sharp-map
  ([upto] (sharp-map upto 0))
  ([upto from]
     "determine a peak or valley in the range from where it crosses up to where it previously crossed"
     (is (>= upto (+ from 7)))
     (map peak-type (take-by 7 (map #(middle %) (range from (+ upto 8)))))))


(defn close? [condition]
					;if distance from violet line is less than ten pips -good
  (<= (abs (- violet  (price))) 10))
(defn momentum? []
  ;;make sure it is still above - wait later for real momentum test!
  (> black violet))


(defn rainbow-scalper []
  (cond
   (cross? large5 small5)
					;wait for large1 and green to actually cross
   (let [condition (wait-for-entrance)]
					;once it crosses, test sharpness and if it is 'close enough' and if momentum is 'good enough/
     (if (and (sharp? condition) (close? condition) (momentum? condition))
       (trade! (opposite trend) price)))
 
   (= (cross? large1 green1 ) trend)
					;determine if large5 and small5 are 'close enought'
   (if (close? large5 small5)
				
					;wait for large1 and green1 to cross back to enter
     (let [condition (wait-for-cross (opposite trend))]
       (if (and (sharp? condition) (close? condition) (momentum? condition))
	 (trade! (opposite trend) price))))))
