(ns forex.backend.mql.service
  (:use utils.general utils.fiber.spawn
	forex.log forex.utils
	forex.backend.mql.constants) 
  (:import (indicators.collection ForexStream))
  (:require [utils.fiber.mbox :as m] 
	    [forex.backend.mql.socket :as s]))
  
(defn get-rel-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is  (>= to from) "in get-data, from/to is invalid")
  (loop [dat nil retries 0]
    (if (> retries 3) (throwf "error %s" (second dat)))
     (let [data (s/receive (format "bars_relative %s %s %s %s" symbol timeframe from to))]
      (if (= (first data) "error") 
	(do (sleep 0.4) (recur data (+ retries 1)))
	data))))
  
(defn get-abs-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is (<= to from) "in get-data, from/to is invalid")
  (loop [dat nil retries 0]
    (if (> retries 3) (throwf "error %s" (second dat)))
    (let [data
	  (s/receive (format "bars_absolute %s %s %s %s"
			     symbol timeframe from to)
		     )]
      (if (= (first data) "error") 
	(do (sleep 0.4) (recur data (+ retries 1)))
	data))))

(defonce- *streams* (ref {}))

(defn- new-price-stream
  ([symbol timeframe] (new-price-stream symbol timeframe 1000))
  ([symbol timeframe max] 
     (let [stream (ForexStream.)]
       (set! (.symbol stream) symbol) (set! (.timeframe stream) timeframe)
       (info "trying to initialize price stream: %s %s "
	     (.symbol stream)  (.timeframe stream))
       (let [dat (get-rel-data (.symbol stream) (.timeframe stream) 0 max)]
         (info "... got data")
	 (on [i (range 0 (+ max 1)) [high low open close]
	        (reverse (group (map #(Double/parseDouble %) (rest dat)) 4))]
	   (.add stream open high low close))
	 (set! (.headTime stream) (Integer/parseInt (first dat)))
	 (info "initialized price stream: %s %s "
	       (.symbol stream)  (.timeframe stream))
	 stream))))

;;TODO: fix BUG::: we cannot use (dosync (get-rel-data "EURUSD" 60 0 10))

(defn get-price-stream [^String symbol ^Integer timeframe]
  (is (and (string? symbol) (integer? timeframe))
      "get-stream: invalid params (%s %s)" symbol timeframe)
  (if (not (get-in @*streams* [symbol timeframe]))
    (let [stream (new-price-stream symbol timeframe)]
      (dosync (when (not (get-in (ensure *streams*) [symbol timeframe]))
		(alter *streams* update-in [symbol timeframe] (fn [a]  stream))))
      stream)
    (get-in @*streams* [symbol timeframe])))


;;TODO: catch errors
(defmacro with-read-lock [l & body]
  `(let [obj# ~l]
     (try (do (.readLock obj#) ~@body) (finally (.readUnlock obj#)))))

(defn- update-all-price-streams [streams]
  (let [all (apply concat (map vals (vals streams)))
	ticks (map #(get-abs-data (.symbol %) (.timeframe %) (abs (now))
				  (.headTime %)) all)]
    (on [tick ticks stream all]
      (let [tt (reverse (group (rest tick) 4))
	    head (first tt)] 
	    (.setHead stream 
		      (Double/parseDouble (nth head 2)) ;;open
		      (Double/parseDouble (first head)) ;;high
		      (Double/parseDouble (second head)) ;;low	
		      (Double/parseDouble (nth head 3))) ;;open high low close
	    (mapc #(do
		     (.add stream ;;open high low close
			   (Double/parseDouble (nth % 2)) ;;open
			   (Double/parseDouble (first %)) ;;high
			   (Double/parseDouble (second %)) ;;low	
			   (Double/parseDouble (nth % 3))) ;;close
		     (.setHead stream (Integer/parseInt (first tick))))
		  (rest tt)) true))))
 
(defn- price-stream-service  [mbox]
  "The stream service constantly upgrades (every 1 second) each price stream. It can receive requests to get open/high/low/close and will initialize and create
it if necessary. It will also 'throw an error' if it receives an error when trying to create it."
  (info "starting price stream service")
  (loop []
    (when (recv-if "stop" nil ? true)
      
	    (try
	      (do (info "attmpeting to update all indicators")
		  (update-all-price-streams @*streams*)
		  (info "... updated all"))
	      (catch Exception e (severe e)))
	    (sleep 1)
	    (recur)))
   (info "stopping price stream service")) 

(defn spawn-price-stream-service []
  (let [mbox (m/new-mbox)]
   {:pid
    (debugging "MQL Price Stream:"
	       (spawn (partial price-stream-service mbox)))
    :mbox mbox}))

(defn stop-price-stream-service [a]
  (debugging "MQL Price Stream:"
	     (if (pid? (:pid a))
	       (! (:pid a) "stop")
	       (warn "stream service already stopped")))) 