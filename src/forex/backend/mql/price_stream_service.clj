;; forex.backend.mql.price_stream_serivce - initialize/update core/*main-streams* with price data and retrieve the resulting ForexStream. Provides a background update service.

(ns forex.backend.mql.price_stream_service
  (:use utils.general utils.fiber.spawn emacs
	forex.util.log forex.util.general) 
  (:import (indicators.collection ForexStream)) 
  (:require [utils.fiber.mbox :as m]
	    [forex.backend.common.core :as core]
	    [forex.backend.mql.socket_service :as s]))
 
;;USER 
(defvar mql-poll-interval 1.0
  "Price stream update poll interval in seconds")
;;

(defn get-rel-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is  (>= to from) "in get-data, from/to is invalid")
  (loop [dat nil retries 0]
    (if (> retries 3) (throwf "MQL error %s" (second dat)))
    (let [data (s/receive (format "bars_relative %s %s %s %s"
				  symbol timeframe from to))]
      (if (= (first data) "error") 
	(do (sleep 0.4) (recur data (+ retries 1)))
	data)))) 
     
(defn get-abs-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is (<= to from) "in get-data, from/to is invalid")
  (loop [dat nil retries 0]
    (if (> retries 3) (throwf "MQL error %s" (second dat)))
    (let [data 
	  (s/receive (format "bars_absolute %s %s %s %s"
			     symbol timeframe from to))]
      (if (= (first data) "error") 
	(do (sleep 0.4) (recur data (+ retries 1)))
	data)))) 

(defn- new-price-stream
  ([symbol timeframe] (new-price-stream symbol timeframe 1000))
  ([symbol timeframe max] 
     (let [stream (ForexStream. symbol timeframe 0)] 
       (info "trying to initialize price stream: %s %s "
	     (.symbol stream)  (.timeframe stream))
       (let [dat (get-rel-data (.symbol stream)
			       (.timeframe stream) 0 max)]
         (info "... got data")
	 (on [i (range 0 (+ max 1)) [high low open close]
	      (reverse (group (map #(Double/parseDouble %) (rest dat)) 4))]
	   (.add stream open high low close))
	 (set! (.headTime stream) (Integer/parseInt (first dat)))
	 (info "initialized price stream: %s %s "
	       (.symbol stream)  (.timeframe stream))
	 stream))))
   
(def get-price-stream
  (mem (fn [symbol timeframe]
	 (let [new-stream (new-price-stream symbol timeframe)]	   
	   (swap! core/*main-streams*
		  assoc (str symbol " " timeframe)
		  new-stream)
	   new-stream))))
 
(defn- update-all-price-streams [streams]
  (let [all (vals streams)
	ticks (map #(get-abs-data (.symbol %) (.timeframe %) (abs (now))
				  (.headTime %)) all)]
    (with-write-lock core/*main-stream-lock*
      (on [tick ticks stream all]
	(let [data (reverse (group (rest tick) 4))
	      head-time (Integer/parseInt (first tick))
	      [high low open close] (first data)]
	  (.setHead stream 
		    (Double/parseDouble open) ;;open
		    (Double/parseDouble high) ;;high
		    (Double/parseDouble low)  ;;low	
		    (Double/parseDouble close)) ;;close
	  (on [[high low open close] (rest data)]
	    (.add stream 
		  (Double/parseDouble open)
		  (Double/parseDouble high)
		  (Double/parseDouble low)
		  (Double/parseDouble close))
	    (set! (.headTime stream) head-time))
	  true)))))

(defn- price-stream-service
  "The stream service upgrades each price stream every mql-poll-interval."
  [mbox]  
  (info "starting price stream service")
  (loop []
    (when (recv-if "stop" nil ? true)  
      (try	      
	(update-all-price-streams @core/*main-streams*)	    		  
	(catch Exception e (severe e)))
      (sleep mql-poll-interval)
      (recur)))
  (info "stopping price stream service"))

(defn spawn-price-stream-service []
  (let [mbox (m/new-mbox)]
   {:pid
    (debugging "MQL Price Stream:"
	       (spawn-log (partial price-stream-service mbox)))
    :mbox mbox}))

(defn stop-price-stream-service [a]
  (debugging "MQL Price Stream:"
	     (if (pid? (:pid a))
	       (! (:pid a) "stop")
	       (warn "stream service already stopped")))) 