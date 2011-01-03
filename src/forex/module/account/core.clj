;;forex.module.order.core - interface with mql backend
  
(ns forex.module.account.core
  (:use utils.general emacs utils.fiber.spawn
	forex.util.general
	forex.module.error.common) 
  (:require [forex.backend.common :as backend]))
 
;;(defn str-to-big [^String s] (java.math.BigDecimal. s))
 


(def- order
  {:buy 0 :sell 1 :buy-limit 2 :sell-limit 3 :buy-stop 4 :sell-stop 5})

(def- color
  {:red 230 :yellow 65535 :green 65280 :blue 13749760
   :purple  16711935 :white 16777215 :black 0})
 
(defn- get! [hash key]
  (if-let [it (hash key)]
    it
    (throwf "invalid key %s in hash %s" key hash)))
 
;;the below can throw errors - how to handle this? 
(defn order-modify
  ([ticket price sl tp]
     (order-modify ticket price sl tp :blue))
  ([ticket price sl tp color_of]
     (receive
      (format "OrderModify %s %s %s %s %s"
	      ticket price sl tp (get! color color_of)))
     ;;{:id ticket :price price :sl sl :tp tp :color color_of}
     ))

(defn order-send
  ([symbol cmd volume price] (order-send symbol cmd volume price 0 0))
  ([symbol cmd volume price sl tp] (order-send symbol cmd volume price sl tp 3))
  ([symbol cmd volume price sl tp slip]
     (receive
      (format "OrderSend %s %s %s %s %s %s %s"
	      symbol (get! order cmd)
	      volume price slip sl tp))))
  
(defn order-close-time [ticket]
  (receive-int (format "OrderCloseTime %s" ticket)))
  
(defn order-close [ticket lots price slippage color_of]
  (receive  
   (format "OrderClose %s %s %s %s %s"
	   ticket lots price slippage (get! color color_of))))
(defn order-delete [ticket]
  (receive (format "OrderDelete %s" ticket)))
 
(defn market-info [symbol type]
  (receive-double (format "MarketInfo %s %s" symbol type)))

(defn order-close-time [ticket]
  (receive-double (format "OrderCloseTime %s" ticket)))

(defn order-type [ticket]
  (receive-double (format "OrderType %s" ticket)))
 
(defn order-lots [ticket]
  (receive-double (format "OrderLots %s" ticket)))

 