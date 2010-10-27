
					;NOTE: error 4054 = no symbol used!
					;NOTE ALSO: one must compiler server.mq4, not just protocol.mq4, or it wont update itself!

(ns forex.server
  (:use clojure.contrib.except clojure.contrib.def)
  (:require [clojure.contrib.str-utils2 :as s])
  (:import
   (java.net Socket)
   (java.io PrintWriter InputStreamReader BufferedReader)))

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

(defonce- *env* (atom {:timeframe +D1+ :index 0}))
(defn env [key] (key @*env*))
(defn env! [map] (swap! *env* #(merge-with (fn [a b] (or b a)) % %2) map))

(defn connect-socket [server]
  (let [socket (Socket. (:name server) (:port server))
	in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
	out (PrintWriter. (.getOutputStream socket))
	conn (ref {:in in :out out :socket socket})] 
    conn))

(defn write-stream [conn msg]
  (throw-if-not conn "no connection provided")
  (doto (:out @conn)
    (.println (str 2 " " msg "\r"))
    (.flush))
  conn)
(defn receive-stream [conn]
  (assert conn)
  (let [result (.readLine (:in @conn))] 
   (rest (s/split result #" +"))))

(defn Receive [msg]
  (write-stream (:socket @*env*) msg)
  (receive-stream (:socket @*env*)))
(defn receive [msg]
  (let [result  (Receive msg)]
    (if (= (first result) "error")
      (throwf (str "error" (second result)))
      result)))
(defonce- *connections* (atom {}))
(defn connect
  ([] (connect 2007))
  ([port-id]
     (let [port (get  @*connections* port-id)]
       (if (and (:socket port) (.isClosed (:socket port))) (swap! *connections* dissoc port-id))
       (if port
	 (throw (Exception. "port already exists and is active - cannot connect!"))
	 (let [socket (connect-socket {:name "localhost" :port port-id})]
	   (env! {:socket socket})
	   (swap! *connections* assoc port-id socket))))))


;;offset in minutes of server time from greg time
(def +offset+ (* 6 60))
;;TODO fix!
(defn date
  ([] (date 0))
  ([index]
     (let [cal (Calendar/getInstance)]
       (.add cal Calendar/MINUTE (+ +offset+ (* -1 (* (env :timeframe) index))))
       (.getTime cal))))

(defn disconnect
  ([] (disconnect 2007))
  ([port-id]
     (let [port (get @*connections* port-id)]
       (when port 
	     (.close (:socket @port)) (.close (:in @port)) (.close (:out @port))
	     (swap! *connections* dissoc port-id)
	     true))))

(defmacro wenv [[ & {symbol :symbol socket :socket timeframe :timeframe index :index}] & body]
  `(binding [*env*  (atom (merge-with #(or %2 %1) @*env* {:symbol ~symbol :socket ~socket :timeframe ~timeframe :index ~index}))] ~@body))


(defmacro wait-for-update [& body]
  `(let [call# (fn [] ~@body)]
     (loop [result# (call#)
	    try# 0]
       (if (and (= (first result#) "error") (= (second result#) "4066"))
	 (do (Thread/sleep 250) (if (< try# 4) (recur (call#) (inc try#)) (throwf "mql4 error %s" (rest result#))))
	 (if (= (first result#) "error") (throwf "mql4 error %s" (rest result#)) result#)))))

(defn iMA [symbol timeframe period mode price index]
  (assert (and (number? timeframe) (string? symbol)))
  (Receive (format "iMA %s %s %s %s %s %s" symbol timeframe period mode price (+ (env :index) index))))
(defn iVMA [symbol timeframe adx weight ma index]
  (assert (and (number? timeframe) (string? symbol))) 
  (Receive (format "FantailVMA %s %s %s %s %s %s" symbol timeframe adx weight ma (+ (env :index) index))))

(defmacro iprocess [& body] `(Double/parseDouble (first (wait-for-update ~@body))))

(defonce- *orders* (atom {}))

(defn record-order [order] 
  (swap! *orders* assoc (:id order) order))

(defn OrderSend [symbol cmd volume price slippage color sl tp]
  (let [num (Double/parseDouble (first (receive (format "OrderSend %s %s %s %s %s %s" symbol cmd volume price slippage color))))
	ticket {:id num :symbol symbol :cmd cmd :sl (or sl 0) :tp (or tp 0) :lots volume :price price :slippage slippage :color color}]
    (record-order ticket)
    ticket))


(defn OrderModify [ticket price sl tp]
  (assert (and (number? ticket) (number? price) (number? sl) (number? tp)))
  (receive (format "OrderModify %f %f %f %f" (double ticket) (double price) (double sl) (double tp))))

(defn buy [{symbol :symbol lots :lots price :price slippage :slippage sl :sl tp :tp}]
  (assert (and (number? lots) (number? price)))
  (let [ticket (OrderSend (or symbol (env :symbol) (throwf "no symbol passed to buy!")) +BUY+ lots price (or slippage 3) "Green" sl tp)]
    (if (or sl tp) (OrderModify num price sl tp))
    ticket))

(defn sell [{symbol :symbol, lots :lots, price :price, slippage :slippage sl :sl tp :tp}]
  (assert (and (number? lots) (number? price)))
  (let [ticket (OrderSend (or symbol (env :symbol) (throwf "no symbol passed to buy!")) +SELL+ lots price (or slippage 3) "Green" sl tp)]
    (if (or sl tp) (OrderModify num price sl tp))
    ticket))
(defn modify [ticket]
  (OrderModify (:id ticket) (:price ticket) (or (:sl ticket) 0) (or (:tp ticket) 0)))

(defn remove-order [id] (swap! *orders*  dissoc  id))
;todo: what? (float 3.5486207E7)
(defn close-order [order]
  (OrderClose (:id order) (:lots order) (:price order) (:slippage order))
  (remove-order (:id order)) true)

(defn OrderClose [id lots price slippage]
  (assert (and (number? id) (number? lots) (number? price) (number? slippage)))
  (receive (format "OrderClose %f %f %f %f RED" (double id) (double lots) (double price) (double slippage))) "true" (pr "HI")
  (remove-order id) true)

(defn order [id] (get  @*orders* id))
(defn order? [ticket] (if (order (:id ticket)) true false))

;(sell {:volume 0.1 :price (close)})

(defn account-equity [] (Double/parseDouble (first (receive "AccountEquity"))))
(defn vma [params & index]
  (assert (and (number? (first params)) (number? (second params)) (number? (second (rest params)))))
  (let [result (fn [index]
		 (iprocess (iVMA (env :symbol) (env :timeframe) (first params) (second params) (second (rest params)) index)))]
    (if index (result (first index)) result)))

(defn mva [period index & {mode :mode price :price}]
  (iprocess (iMA (env :symbol) (env :timeframe) period (or mode +MODE_SMA+) (or price +CLOSE+) index)))


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


;test!
(defn vma-cross? []
 (let [large (vma '(2 2 100))
       small (vma '(2 2 1))]
   (let [large_prev (large 1) large_current (large 0)
	 small_prev (small 1) small_current (small 0)]
     (cond (and (> large_prev small_prev) (<= large_current small_current)) :up
	   (and (< large_prev small_prev) (>= large_current small_current)) :down))))
