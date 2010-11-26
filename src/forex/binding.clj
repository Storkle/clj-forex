
					;NOTE: error 4054 = no symbol used!
					;NOTE ALSO: one must compiler server.mq4, not just protocol.mq4, or it wont update itself!

					;note: if one uses the script for daily timeframe, wont work!
 

(ns forex.binding
  (:refer-clojure :exclude (=))
  (:use forex.utils forex.socket)
  (:import (org.joda.time Instant DateTime DateTimeZone Interval)))

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

(defn now [] (DateTime. DateTimeZone/UTC))
 
(defn abs
  ([] (int (/ (.getMillis (Instant. (now))) 1000)))
  ([date] (int (/ (.getMillis (Instant. date)) 1000))))

(defn get-rel-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is?  (>= to from) "in get-data, from/to is wrong")
  (loop [dat nil retries 0]
    (if (> retries 3) (throwf "error %s" (second dat)))
    (let [data (receive (format "bars_relative %s %s %s %s" symbol timeframe from to))]
      (if (is (first data) "error") 
	(do (Thread/sleep 400) (recur data (+ retries 1)))
	data))))

(defn get-abs-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is? (<= to from) "in get-data, from/to is wrong")
  (loop [dat nil retries 0]
    (if (> retries 3) (throwf "error %s" (second dat)))
    (let [data (receive (format "bars_absolute %s %s %s %s" symbol timeframe from to))]
      (if (is (first data) "error") 
	(do (Thread/sleep 400) (recur data (+ retries 1)))
	data))))


(import 'forex.indicator.core.ForexStream)

(defonce- *streams* (atom {}))


(defn head [stream] (if (zero? (.getHead stream)) (abs (now)) (.getHead stream)))
(defn out [s & args]
  (println (apply format (str "[] " s) args)))

(defn new-stream
  ([symbol timeframe] (new-stream symbol timeframe 1000))
  ([symbol timeframe max] 
     (let [stream (ForexStream. symbol timeframe)]
       (out "initializing price stream: %s %s " (.symbol stream)  (.timeframe stream))
       (.reset stream)  
       (let [dat (get-rel-data (.symbol stream) (.timeframe stream) 0 max)]
	 (on [i (range 0 (+ max 1)) [high low open close] (reverse (group (map #(Double/parseDouble %) (rest dat)) 4))]
	   (.put stream i high low open close))
	 (.setHead stream (Integer/parseInt (first dat)))
	 stream))))

(defn get-stream [^String symbol ^Integer timeframe]
  (is? (and (string? symbol) (integer? timeframe))
       "get-stream: invalid params (%s %s)" symbol timeframe)
  (if (not (get-in @*streams* [symbol timeframe]))
    (let [stream (new-stream symbol timeframe)]
      (swap! *streams* update-in [symbol timeframe] (fn [a]  stream))
      stream)
    (get-in @*streams* [symbol timeframe])))

(defonce *indicators* (atom {}))

(defn ihigh [i symbol timeframe]
  (let [s (get-stream symbol timeframe)]
    (.high s i)))
(defn iopen [i symbol timeframe]
  (let [s (get-stream symbol timeframe)]
    (is? s "no stream available for symbol %s and timeframe %s" symbol timeframe)
    (.open s i)))
(defn ilow [i symbol timeframe] 
  (let [s (get-stream symbol timeframe)]
   (is? s "no stream available for symbol %s and timeframe %s" symbol timeframe)
    (.low s i)))
(defn iclose [i symbol timeframe]
  (let [s (get-stream symbol timeframe)]
     (is? s "no stream available for symbol %s and timeframe %s" symbol timeframe)
    (.close s i)))
 
(defn update-streams [streams]
  (let [all (apply concat (map vals (vals streams)))
	ticks (map #(let [data (get-abs-data (.symbol %) (.timeframe %) (abs (now)) (head %))]
		      (if (is (first data) "error")
			(throwf "error in updating streams")
			data)) all)]
    (on [tick ticks stream all]
      (mapc #(do
	       (.put stream %2
		     (Double/parseDouble (first %))	     ;high
		     (Double/parseDouble (second %))	     ;low
		     (Double/parseDouble (nth % 2))	     ;open
		     (Double/parseDouble (nth % 3)))	     ;close
	       (.setHead stream (Integer/parseInt (first tick))))
	    (reverse (group (rest tick) 4)) 
	    (iterate inc (- (let [size (.size stream)]
			      (if (zero? size) 1 size)) 1))))))


;(def val (get-rel-data "EURUSD" 60 0 100))

;;;;indicator test!!!!
