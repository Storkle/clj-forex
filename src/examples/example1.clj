(ns forex.examples.example1 (:use forex.server))
;;example usage
(defn c?
  ([direction tp] (c? direction tp 0))
  ([direction tp index]
     (let [h (+ (high index) (points 5))
	   l (- (low index) (points 5))
	   [price sl] (cond (= direction :up) (list h l)
			    (= direction :down) (list l h)
			    true (throwf "give me up or down!"))
	   risk (Math/abs (- price sl))
	   reward (Math/abs (- price tp))]
       (print (format "PRICE %s%nTP %s%nSL %s%nRISK %s%nREWARD %s%nR/R %s" price tp sl (pips risk) (pips reward) (/ reward risk))))))

;;usage
(comment
  ;;basically, you can either globally set the environment like this
  (env! {:symbol "USDJPY" :timeframe +D1+})
  (print (str "low back 1 is " (low 1)))
					;or you can set the environment in a 'scope'
  (wenv (:symbol "USDJPY" :timeframe +D1+) (print (str "low back 1 is " (low 1))))
  ;;we first have to connect to the server by calling
  (connect)

  ;;and we will add more later! the metatrarder server actually has some more protocols programmed into it ....
  )
