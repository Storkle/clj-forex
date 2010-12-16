;;This allows us to update our price stream and indicators by calling (refresh-rates). It also allows us to create 'contextes' from which we can update
;;from our main price stream. Get-price-stream will get price stream. indicator-naive-cache-strategy will cache all of our indicators that we will create
;;elsewhere.

;;exporting get-stream,refresh-rates,context,indicator-naive-cache-strategy
(ns forex.backend.common.service
  (:use forex.backend.common.core 
        utils.general utils.fiber.spawn
	forex.util.general)
  (:require [forex.backend.common.core :as core])
  (:import (indicators.collection ForexStream))) 

;;TODO: make into macro
(deftype IndicatorNaiveStrategy [cache]
  PCachingStrategy
  (retrieve 
   [_ item]
   (get @cache item)) 
  (cached?
   [_ item]
   (contains? @cache item))
  (hit
   [this _]
   this)
  (miss 
   [_ item result]
   (with-write-lock core/*indicator-lock* (.ex (force result)))
   (swap! core/*indicators* assoc item result)
   (IndicatorNaiveStrategy. core/*indicators*)))

(defn indicator-naive-cache-strategy
  "The naive safe-all cache strategy for memoize."
  []
  (IndicatorNaiveStrategy. core/*indicators*))  

(deftype ^{:private true} PriceStreamNaiveStrategy [cache]
  PCachingStrategy
  (retrieve 
   [_ item]
   (first (get @cache item)))
  (cached?
   [_ item]
   (contains? @cache item))
  (hit
   [this _]
   this)
  (miss 
   [_ item result]
   (swap! core/*streams* assoc item result)
   (PriceStreamNaiveStrategy. core/*streams*)))

(defn- price-stream-naive-cache-strategy
  "The naive safe-all cache strategy for memoize."
  []
  (PriceStreamNaiveStrategy. core/*streams*))

(def get-stream
  (mem (fn [symbol timeframe] (ForexStream. (core/get-price-stream core/*backend* symbol timeframe)))
       (price-stream-naive-cache-strategy)))

(defn- update-all-price-streams []
  (with-write-lock core/*main-stream-lock*
    (with-read-lock core/*stream-lock*
      (on [stream (vals @core/*streams*)]
	(when-not (atom? stream)
	  (.update (first stream) (second stream))))))) 
 
(defn- update-all-indicators []
  (with-write-lock core/*stream-lock*
    (on [ind (vals @core/*indicators*)]
      (.ex (force ind))))) 

(defn refresh-rates []
  (update-all-price-streams)
  (update-all-indicators))
 
(defmacro context [& body]
  `(binding [core/*indicators* (atom {})
	     core/*streams* (atom {}) 
	     core/*stream-lock* (java.util.concurrent.locks.ReentrantReadWriteLock.)]
     ~@body))

