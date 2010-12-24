;;forex.module.order.core - interface with mql backend
 
(ns forex.module.account.core
  (:use utils.general emacs utils.fiber.spawn
	forex.util.general
	forex.backend.mql.socket_service)
  (:require [forex.backend.common :as backend]
	    [forex.module.indicator.common :as ind]))
 
;;(defn str-to-big [^String s] (java.math.BigDecimal. s))

(defn sym [a] (symbol (camel-to-dash a)))
(defmacro- single [name] `(defn ~(sym name) [] (receive-str! ~name)))
(defmacro- double-single [name] `(defn ~(sym name) [] (receive-double! ~name)))
(defmacro- singles [& names] `(do ~@(map (fn [a] `(single ~a)) names)))
(defmacro- double-singles [& names] `(do ~@(map (fn [a] `(double-single ~a)) names)))

(singles
 "AccountCurrency"
 "AccountCompany"
 "AccountServer" 
 "AccountName"
 "AccountNumber")

(double-singles
 "AccountCredit"
 "AccountBalance"
 "AccountEquity"
 "AccountFreeMargin"
 "AccountLeverage"
 "AccountMargin"
 "AccountProfit"
 "OrdersTotal")

(def- order
  {:buy 0 :sell 1 :buy-limit 2 :sell-limit 3 :buy-stop 4 :sell-stop 5})

(def- color
  {:red 230 :yellow 65535 :green 65280 :blue 13749760
   :purple  16711935 :white 16777215 :black 0})

(defn- get! [hash key]
  (if-let [it (hash key)]
    it
    (throwf "invalid key %s in hash %s" key hash)))
 
;;(order-send "USDJPY" :sell 0.1 82.927 4)
(defn order-modify
  ([ticket price sl tp]
     (order-modify ticket price sl tp :blue))
  ([ticket price sl tp color_of]
     (receive! (format "OrderModify %s %s %s %s %s"
		       ticket price sl tp (get! color color_of)))
     {:id ticket :price price :sl sl :tp tp :color color_of}))

(defn order-send
  ([symbol cmd volume price] (order-send symbol cmd volume price 0 0))
  ([symbol cmd volume price sl tp] (order-send symbol cmd volume price sl tp 3))
  ([symbol cmd volume price sl tp slip]
     (receive-str!
      (format "OrderSend %s %s %s %s %s %s %s"
	      symbol (get! order cmd)
	      volume price slip sl tp))))
 
(defn receive-int! [s]
  (int (receive-double! s)))

(defn order-close-time [ticket]
  (receive-int! (format "OrderCloseTime %s" ticket)))

(defn order-close [ticket lots price slippage color_of]
  (receive-str!
   (format "OrderClose %s %s %s %s %s"
	   ticket lots price slippage (get! color color_of))))
(defn order-delete [ticket]
  (receive-str! (format "OrderDelete %s" ticket)))
(defn market-info [symbol type]
  (receive-double! (format "MarketInfo %s %s" symbol type)))
(defn order-close-time [ticket]
  (receive-double! (format "OrderCloseTime %s" ticket)))
(defn order-type [ticket]
  (receive-double! (format "OrderType %s" ticket)))

(defn order-lots [ticket]
  (receive-double! (format "OrderLots %s" ticket)))

