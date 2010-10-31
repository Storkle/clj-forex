
					;NOTE: error 4054 = no symbol used!
					;NOTE ALSO: one must compiler server.mq4, not just protocol.mq4, or it wont update itself!

					;note: if one uses the script for daily timeframe, wont work!



(ns forex.binding
  (:use forex.socket forex.utils))


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


(defmacro wait-for-update [& body]
  `(let [call# (fn [] ~@body)]
     (loop [result# (call#)
	    try# 0]
       (if (and (= (first result#) "error") (= (second result#) "4066"))
	 (do (Thread/sleep 250) (if (< try# 4) (recur (call#) (inc try#)) (throwf "mql4 error %s" (rest result#))))
	 (if (= (first result#) "error") (throwf "mql4 error %s" (rest result#)) result#)))))


(defmacro iprocess [& body] `(Double/parseDouble (first (wait-for-update ~@body))))
(defn iMA [symbol timeframe period mode price index]
  (is (and (number? timeframe) (string? symbol)))
  (Receive (format "iMA %s %s %s %s %s %s" symbol timeframe period mode price (+ (env :index) index))))
(defn iVMA [symbol timeframe adx weight ma index]
  (is (and (number? timeframe) (string? symbol))) 
  (Receive (format "FantailVMA %s %s %s %s %s %s" symbol timeframe adx weight ma (+ (env :index) index))))
(defn iATR [symbol timeframe period index]
  (is (and (integer? timeframe) (integer? period) (string? symbol)))
  (Receive (format "iATR %s %s %s %s" symbol timeframe period (+ (env :index) index))))

(defn atr
  ([period] (atr period 0))
  ([period index]
     (iprocess (iATR (env :symbol) (env :timeframe) period index))))

(defn mva
  ([period] (mva period 0))
  ([period index & {mode :mode price :price}]
     (iprocess (iMA (env :symbol) (env :timeframe) period (or mode +MODE_SMA+) (or price +CLOSE+) index))))

(defn vma [params & index]
  (is (and (number? (first params)) (number? (second params)) (number? (second (rest params)))))
  (let [result (fn [index]
		 (iprocess (iVMA (env :symbol) (env :timeframe) (first params) (second params) (second (rest params)) index)))]
    (if index (result (first index)) result)))




(defonce- *orders* (atom {}))

(defn record-order [order] 
  (swap! *orders* assoc (:id order) order))

(defn OrderSend [symbol cmd volume price slippage color sl tp]
  (let [num (Double/parseDouble (first (receive (format "OrderSend %s %s %s %s %s %s" symbol cmd volume price slippage color))))
	ticket {:id num :symbol symbol :cmd cmd :sl (or sl 0) :tp (or tp 0) :lots volume :price price :slippage slippage :color color}]
    (record-order ticket)
    ticket))


(defn OrderModify [ticket price sl tp]
  (is (and (number? ticket) (number? price) (number? sl) (number? tp)))
  (receive (format "OrderModify %f %f %f %f" (double ticket) (double price) (double sl) (double tp))))

(defn buy [{symbol :symbol lots :lots price :price slippage :slippage sl :sl tp :tp}]
  (is (and (number? lots) (number? price)))
  (let [ticket (OrderSend (or symbol (env :symbol) (throwf "no symbol passed to buy!")) +BUY+ lots price (or slippage 3) "Green" sl tp)]
    (if (or sl tp) (OrderModify num price sl tp))
    ticket))

(defn sell [{symbol :symbol, lots :lots, price :price, slippage :slippage sl :sl tp :tp}]
  (is (and (number? lots) (number? price)))
  (let [ticket (OrderSend (or symbol (env :symbol) (throwf "no symbol passed to buy!")) +SELL+ lots price (or slippage 3) "Green" sl tp)]
    (if (or sl tp) (OrderModify num price sl tp))
    ticket))
(defn modify [ticket]
  (OrderModify (:id ticket) (:price ticket) (or (:sl ticket) 0) (or (:tp ticket) 0)))

(defn remove-order [id] (swap! *orders*  dissoc  id))
;todo: what? (float 3.5486207E7)

(defn OrderClose [id lots price slippage]
  (is (and (number? id) (number? lots) (number? price) (number? slippage)))
  (receive (format "OrderClose %f %f %f %f RED" (double id) (double lots) (double price) (double slippage))) "true" (pr "HI")
  (remove-order id) true)

(defn close-order [order]
  (OrderClose (:id order) (:lots order) (:price order) (:slippage order))
  (remove-order (:id order)) true)

(defn order [id] (get  @*orders* id))
(defn order? [ticket] (if (order (:id ticket)) true false))

;(sell {:volume 0.1 :price (close)})

(defn account-equity [] (Double/parseDouble (first (receive "AccountEquity"))))

(defn jpy? [] (re-find #"JPY" (env :symbol)))
(defn point [] (if (jpy?) 0.01 0.0001))
(defn dollar [p] (/ p (point)))
(defn pips [p] (* p (point)))


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

