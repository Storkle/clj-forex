;;TODO: if indicators have zeros at end - should we filter these out??? or just let them be zero, like we already do if it is out of scope of vector?

(clojure.core/use 'nstools.ns)
(ns+ forex.module.indicator 
     (:clone clj.core)
     (:use forex.module.indicator.shorthand
	   forex.module.indicator.util)
     (:use forex.util.core forex.util.general
	   forex.module.error
	   forex.module.account.utils)
     (:require clojure.contrib.core
      [forex.backend.mql.socket-service :as backend])) 

(defn ibarshift
  ([] (ibarshift 0))
  ([i & e]
     {:pre [(integer? i)]}
     (let [{:keys [symbol period]} (env-dispatch e true)]
       (receive! (format "iBarShift %s %s %s"
			 symbol period i)))))
(defn itime
  ([] (itime 0))
  ([i & e]
     {:pre [(integer? i)]}
     (let [{:keys [symbol period]} (env-dispatch e true)]
       (receive! (format "iTime %s %s %s" symbol period
			 i))))) 
(defn ibars 
  ([& e]
     (let [{:keys [symbol period]} (env-dispatch e true)]
       (receive! (format "iBars %s %s" symbol period)))))

(def +m1+ 1)
(def +m5+ 5)
(def +m30+ 30)
(def +h1+ 60)
(def +h4+ 240)
(def +d1+ 1440)
(def +w1+ 10080)
(def +mn1+ 43200)

(defmacro- mva [name val]
  (is (integer? val))
  (let [ifn-name (symbolicate "i" name)]
   `(do (defn ~name [period# & args#]
	  (apply moving-averages [period# 0 ~val] args#))
	(defn ~ifn-name [period# & args#]
	  (apply imoving-averages [period# 0 ~val] args#))
	(set-arglists! #'~name #'moving-averages)
	(set-arglists! #'~ifn-name #'moving-averages))))

(def-indicator [close "iClose"])
(def-indicator [open "iOpen"])
(def-indicator [high "iHigh"])
(def-indicator [low "iLow"])

(def-indicator [moving-averages "Default_Moving_Averages"] true)
(mva sma 0)
(mva ema 1)
(mva smma 2)
(mva lwma 3)

(def-indicator [psar "Default_Parabolic"] true)
(def-indicator [cci "Default_CCI"] true)
(def-indicator [momentum "Default_Momentum"] true)
(def-indicator [atr "Default_ATR"] true)
(def-indicator [rsi "Default_RSI"] true)
(def-indicator [vma "Custom_FantailVMA3"] true) 
(def-indicator [blazan-dynamic-stop "Custom_Blazan_Dynamic_Stop"] true true)


(defn ask [& e]
  (aif (mode-ask (:symbol (env-dispatch e))) it (throwf e)))
(defn bid [& e]
  (aif (mode-bid (:symbol (env-dispatch e))) it (throwf "MQL error %s" (:e it))))
  
(defn cross?
  ([signal main] (cross? signal main 0))
  ([signal main i]
     (let [i1 (+ i 1) i2 (+ i 2)
	   a1 (signal i1) a2 (signal i2)
	   b1 (main i1) b2 (main i2)] 
       (or (and (> a1 b1) (< a2 b2) :buy)
	   (and (< a1 b1) (> a2 b2) :sell)
	   nil))))

(defn hit? [order val]
  (cond
   (sell? order)
   (<= (close) val)
   (buy? order)
   (>= (close) val) 
   true (throwf "invalid order type %s" order)))

(defn risk
  ([percent sl] (risk percent sl (close)))
  ([percent sl price] (risk percent sl price (env :symbol)))
  ([percent sl price symbol]
     (wenv {:symbol symbol}
	   (lot (* 0.1 (/ (* (/ percent 100) (account-balance))
			  (point (Math/abs (- price sl)))))))))


