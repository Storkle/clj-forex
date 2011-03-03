
;;forex.module.order.core - interface with mql backend
(clojure.core/use 'nstools.ns)
(ns+ forex.module.account.core
  (:clone clj.core)
  (:use forex.util.core
        forex.util.emacs 
        forex.util.general
        forex.module.error))

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
     (receive-critical
      (format "OrderModify %s %s %s %s %s"
	      (first ticket)
	      price sl tp (get! color color_of))))) 

(defn order-send
  ([symbol cmd volume price] (order-send symbol cmd volume price 0 0))
  ([symbol cmd volume price sl tp] (order-send symbol cmd volume price sl tp 3))
  ([symbol cmd volume price sl tp slip]
     (receive-critical
      (format "OrderSend %s %s %s %s %s %s %s"
              symbol (get! order cmd)
              volume price slip sl tp))))

(defn order-close [ticket lots price slippage color_of]
  (receive-critical
   (format "OrderClose %s %s %s %s %s"
	   (first ticket) lots price slippage (get! color color_of))))
(defn order-delete [ticket]
  (receive-critical (format "OrderDelete %s" (first ticket))))

(defn order-close-time [ticket]
  (receive (format "OrderCloseTime %s" (first ticket))))

(defn market-info [symbol type]
  (receive (format "MarketInfo %s %s" symbol type)))

(defn order-close-time [ticket]
  (receive (format "OrderCloseTime %s" (first ticket))))

(defn order-type [ticket]
  (receive (format "OrderType %s" (first ticket))))

(defn order-lots [ticket]
  (receive (format "OrderLots %s" (first ticket))))


