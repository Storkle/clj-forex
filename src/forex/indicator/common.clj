;; forex.indicator.common - interfacing with java indicators and local caching of indicators
(ns forex.indicator.common
   (:use utils.general forex.util.general
	 forex.backend.common)
   (:require [ forex.backend.common.core :as common]))

 (defn open
    ([] (open 0))  
    ([i] (.open (get-stream (env :symbol) (env :timeframe)) i)))
  (defn high
    ([] (high 0))
    ([i]
       (.high (get-stream (env :symbol) (env :timeframe)) i))) 
  (defn low
    ([] (low 0)) 
    ([i]
       (.low (get-stream (env :symbol) (env :timeframe)) i))) 
  (defn close
    ([] (close 0)) 
    ([i] 
       (.close (get-stream (env :symbol) (env :timeframe)) i)))


(comment
  (ns forex.indicator.common
   (:use utils.general forex.util.general
	 forex.backend.common)
   (:require [ forex.backend.common.core :as common])
   (:import (indicators SMA RSI CCI ATR VMA EMA)))

;;Indicator Cache Strategy - if indicator is deinitialized, then we must get rid of it! ???

  (defmacro mem-local-indicator [function cache-var results-var]
    `(mem (fn [indicator-name# & args#]
	    (let [result# (apply ~function args#)]
	      (swap! ~results-var assoc (list indicator-name# args#) result#)
	      (.ex result#)
	      result#))
	  (naive-var-local-cache-strategy ~cache-var)))
   
  (defn def-indicator [name create]
    (let [memoize-create (mem-local-indicator
			  create common/*indicators-cache*
			  common/*indicators*)]
      (fn indicator
	([params] (indicator params 0))
	([params index]
	   (let [price-stream (get-stream (env :symbol) (env :timeframe))
		 stream (memoize-create name price-stream params)] 
	     (if index
	       (with-read-lock common/*indicator-lock* (.get stream index)) 
	       (fn ind ([] (ind 0))
		 ([index] (with-read-lock common/*indicator-lock*
			    (.get stream index))))))))))

  (defn def-price-indicator [name create]
    (let [memoize-create
	  (mem-local-indicator
	   create
	   common/*indicators-cache*
	   common/*indicators*)]
      (fn indicator
	([params] (indicator params 0))
	([params index]
	   (let [price-stream (get-stream (env :symbol) (env :timeframe))
		 stream (memoize-create name price-stream
					(.Close price-stream) params)]
	     (if index
	       (with-read-lock common/*indicator-lock* (.get stream index))
	       (fn ind ([] (ind 0))
		 ([index] (with-read-lock common/*indicator-lock*
			    (.get stream index))))))))))

  (defn open
    ([] (open 0))  
    ([i] (.open (get-stream (env :symbol) (env :timeframe)) i)))
  (defn high
    ([] (high 0))
    ([i]
       (.high (get-stream (env :symbol) (env :timeframe)) i))) 
  (defn low
    ([] (low 0)) 
    ([i]
       (.low (get-stream (env :symbol) (env :timeframe)) i))) 
  (defn close
    ([] (close 0)) 
    ([i] 
       (.close (get-stream (env :symbol) (env :timeframe)) i)))

;;TODO: if indicator is 'deinitialized' - clear from cache!

  (def sma (def-price-indicator 'sma
	     (fn [price stream period] (SMA. price stream period))))
  (def ema (def-price-indicator 'ema
	     (fn [price stream period] (EMA. price stream period))))
  (def rsi (def-price-indicator 'rsi
	     (fn [price stream period] (RSI. price  stream period))))
  (def atr (def-indicator 'atr
	     (fn [price period] (ATR. price period))))
  (def cci (def-indicator 'cci
	     (fn [price period] (CCI. price period))))
  (def vma (def-indicator 'vma
	     (fn [price [adx_period weight period]]
	       (VMA. price adx_period weight period))))
  
;;BUG IN VMA - paramaters [2 2 1]


  )