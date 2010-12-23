;;forex.module.order.core - interface with mql backend
 
(ns forex.module.order.core
  (:use utils.general emacs utils.fiber.spawn
	forex.util.general
	forex.backend.mql.socket_service)
  (:require [forex.backend.common :as backend]
	    [forex.module.indicator.common :as ind]))
 
;;(defn str-to-big [^String s] (java.math.BigDecimal. s))
(defn receive-double [s] (Double/parseDouble (first (receive s))))
(defn receive-str [s] (first (receive s)))

(defn sym [a] (symbol (camel-to-dashed a)))
(defmacro- single [name] `(defn ~(sym name) [] (receive-str ~name)))
(defmacro- double-single [name] `(defn ~(sym name) [] (Double/parseDouble (first (receive ~name)))))
(defmacro- singles [& names] `(do ~@(map (fn [a] `(single ~a)) names)))
(defmacro- double-singles [& names] `(do ~@(map (fn [a] `(double-single ~a)) names)))

(singles
 "AccountCurrency"
 "AccountCompany"
 "AccountServer"
 "AccountName")

(float-singles
 "AccountCredit"
 "AccountBalance"
 "AccountEquity"
 "AccountFreeMargin"
 "AccountLeverage"
 "AccountMargin"
 "AccountProfit"
 "AccountNumber"
 "OrdersTotal")
 
(defn order-send
  ([symbol cmd volume price] (order-send symbol cmd volume price 3))
  ([symbol cmd volume price slippage] (order-send symbol cmd volume price slippage 0 0))
  ([symbol cmd volume price slippage stoploss takeprofit]
     (receive-str (format "OrderSend %s %s %s %s %s %s %s" symbol cmd volume price slippage stoploss takeprofit))))
(defn order-close [ticket lots price slippage color_of]
  (receive-str (format "OrderClose %s %s %s %s %s" ticket lots price slippage color_of)))
(defn order-delete [ticket] (receive-str (format "OrderDelete %f" ticket)))
(defn market-info [symbol type] (receive-double (format "MarketInfo %s %s" symbol type)))
(defn order-close-time [ticket] (receive-double (format "OrderCloseTime %s" ticket)))
(defn order-type [ticket] (receive-double (format "OrderType %s" ticket)))
(defn order-lots [ticket] (receive-double (format "OrderLots %s" ticket)))


(comment
  (.generate (new-order "buy" :sl sl :tp tp))
  (let [order (new-order "buy" :sl sl :tp tp)]
    (.generate order)
    (.generate (order-modify order {:lots .1})) 
    (order-close order)
    (order-open? order)))

