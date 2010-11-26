(ns forex.indicator
  (:refer-clojure :exclude (=))
  (:import (forex.indicator SMA EMA CCI ATR FantailVMA RSI))
  (:use forex.utils forex.binding forex.indicator_core))
  
;;TODO: allow different strategies for hashing!???
;;TODO: though right now we are only utilizing 1000 bars per price stream and thus indicator, do we really need to calculate more if we get more price streams?
;; in some cases - yes; others - no?
                                                               
(def sma (price-indicator1 'sma (fn [[stream price period]] (SMA. stream price period))))
(def rsi (price-indicator1 'rsi (fn [[stream price period]] (RSI. stream price period))))
(def ema (price-indicator1 'ema (fn [[stream price period]] (EMA. stream price period))))
(def cci (indicator1 'cci (fn [[stream period]] (CCI. stream period))))
(def atr (indicator1 'atr (fn [[stream period]] (ATR. stream period))))

(def vma (indicator
	  'vma
	  (fn [[stream  adx-period weight period]]
	    (FantailVMA. stream adx-period weight period))
	  (fn [obj index] (.get (.average obj) index))))





 