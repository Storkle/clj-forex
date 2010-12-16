(ns forex.indicator.common
  (:use utils.general forex.util.general
	forex.backend.common)
  (:require [ forex.backend.common.core :as common])
  (:import (indicators SMA RSI CCI ATR VMA EMA)))
         
(defn def-indicator [create]
  (let [memoize-create (mem create (indicator-naive-cache-strategy))]
    (fn indicator
      ([params] (indicator params 0))
      ([params index]
	 (let [price-stream (get-stream (env :symbol) (env :timeframe))
	       stream (memoize-create price-stream params)] 
	   (if index
	     (with-read-lock common/*indicator-lock* (.get stream index)) 
	     (fn ind ([] (ind 0))
	       ([index] (with-read-lock common/*indicator-lock*
			  (.get stream index))))))))))

(defn def-price-indicator [create]
  (let [memoize-create (mem create  (indicator-naive-cache-strategy))]
    (fn indicator
      ([params] (indicator params 0))
      ([params index]
	 (let [price-stream (get-stream (env :symbol) (env :timeframe))
	       stream (memoize-create price-stream (.Close price-stream) params)]
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

(def sma (def-price-indicator
	   (fn [price stream period] (SMA. price stream period))))
(def ema (def-price-indicator
	   (fn [price stream period] (EMA. price stream period))))
(def rsi (def-price-indicator
	   (fn [price stream period] (RSI. price  stream period))))
(def atr (def-indicator
	   (fn [price period] (ATR. price period))))
(def cci (def-indicator
	   (fn [price period] (CCI. price period))))
(def vma (def-indicator
	   (fn [price [adx_period weight period]]
	     (VMA. price adx_period weight period))))


