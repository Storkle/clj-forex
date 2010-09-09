;;FUTURE GOALS
;;this code will be developed modularly and goal oriented
;; i want an ea which will run continuosly, and 'survive' internet faulty connections, power shutoff, etc.
;; i want to be able to query the status of the ea. i want to have a graphical interface in my toolbar in which i can get any logs, messages
;; i want the option to have an alert box which will pop up, just like in metatrader, it will contain all alerts!

;;this ea will be simple, in terms of logic - not something which trades, but maybe warns when 2 eas cross over! (with the option to trade i suppose!)
;;that is the goal ....

(ns server
  (:use clojure.contrib.except clojure.contrib.def)
  (:import
   (java.net Socket)
   (java.io PrintWriter InputStreamReader BufferedReader)))
(require '[clojure.contrib.str-utils2 :as s])

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

(constants
  +M1+ 1
  +M5+ 5
  +M15+ 15
  +M30+ 30
  +H1+ 60
  +H4+ 240
  +D1+ 1440
  +W1+ 10080
  +MN1+ 43200
  
  +MODE_SMA+ 0
  +MODE_EMA+ 1
  +MODE_SMMA+ 2
  +MODE_LWMA+ 3
  
  +CLOSE+ 0
  +OPEN+ 1
  +HIGH+ 2
  +LOW+ 3
  +MEDIAN+ 4
  +TYPICAL+ 5
  +WEIGHTED+ 6

  +MODE_DIGITS+ 12
  +MODE_SPREAD+ 13
  +MODE_STOPLEVEL+ 14
  +MODE_LOTSIZE+ 15
  +MODE_POINT+ 11
  +MODE_BID+ 9
  +MODE_ASK+ 10

  +BUY+ 0
  +SELL+ 1
  +BUYLIMIT+ 2
  +SELLLIMIT+ 3
  +BUYSTOP+ 4
  +SELLSTOP+ 5)

(defvar *env* (atom {:timeframe +D1+}))
(defn env [key] (key @*env*))
(defn env! [map] (swap! *env* #(merge-with (fn [a b] (or b a)) % %2) map))

(defn connect-socket [server]
  (let [socket (Socket. (:name server) (:port server))
	in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
	out (PrintWriter. (.getOutputStream socket))
	conn (ref {:in in :out out})]
    conn))
(defn write-stream [conn msg]
  (throw-if-not conn "no connection provided")
  (doto (:out @conn)
    (.println (str 2 " " msg "\r"))
    (.flush))
  conn)
(defn receive-stream [conn]
  (assert conn)
  (rest (s/split (.readLine (:in @conn)) #" +")))

(defn receive [msg]
  (write-stream (:socket @*env*) msg)
  (receive-stream (:socket @*env*)))

;;we now can use the sockets - now we need
;;mql error handling and parsing numbers if no error!
(def metatrader {:name "localhost" :port 2007})
(defn connect []
  (env! {:socket (connect-socket metatrader)}))
(defmacro wenv [[ & {symbol :symbol socket :socket timeframe :timeframe}] & body]
  `(binding [*env*  (atom (merge-with #(or %2 %1) @*env* {:symbol ~symbol :socket ~socket :timeframe ~timeframe}))] ~@body))

(defn iMA [symbol timeframe period mode price index]
  (receive (format "iMA %s %s %s %s %s %s" symbol timeframe period mode price index)))

(defmacro wait-for-update [& body]
  `(let [call# (fn [] ~@body)]
     (loop [result# (call#)
	    try# 0]
       (if (and (= (first result#) "error") (= (second result#) "4066"))
	 (do (Thread/sleep 250) (if (< try# 4) (recur (call#) (inc try#)) (throwf "mql4 error %s" (rest result#))))
	 (if (= (first result#) "error") (throwf "mql4 error %s" (rest result#)) result#)))))
(defn jpy? [] (re-find #"JPY" (env :symbol)))
(defn pt [] (if (jpy?) 0.01 0.0001))
(defn pips [p] (/ p (pt)))
(defn points [p] (* p (pt)))

(defn mva [period index & {mode :mode price :price}]
  (Float/parseFloat
   (first (wait-for-update (iMA (env :symbol) (env :timeframe) period (or mode +MODE_SMA+) (or price +CLOSE+) index)))))

(defn close
  ([] (close 0))
  ([index] (mva 1 index :price +CLOSE+)))
(defn open
  ([] (open 0))
  ([index] (mva 1 index :price +OPEN+)))
(defn high
  ([] (high 0))
  ([index] (mva 1 index :price +HIGH+)))
(defn low
  ([] (low 0))
  ([index] (mva 1 index :price +LOW+)))

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

