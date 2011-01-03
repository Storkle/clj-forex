;;forex.module.order.common - api for creating/modify orders
  
(ns forex.module.account.common
  (:use utils.general emacs  
	forex.util.general
	forex.module.error.common)
  (:require [forex.module.error.common :as err])
  (:require  [forex.module.account.core :as core]))

(defmacro- default [& body] 
  `(binding [*default* @~#'err/*er*]
     ~@body))
  
;;TODO: do we need all of these assertions?

(def- value-to-order-type
  {0 :buy 1 :sell 2 :buy-limit
   3 :sell-limit 4 :buy-stop
   5 :sell-stop}) 
 
(defn order-close-time [{id :id}]
  (is (string? id))
  ;;we dont need to know mql4 error codes for order close time
  (iff (core/order-close-time id) it -1))

(defn open? [order]
  (default (= (order-close-time order) 0)))
   
(defn order-type
  "type of order, even if it is already closed"
  [order]
  (is (string? (:id order)))
  (default
    (iff (core/order-type (:id order))
	 (value-to-order-type (int it)))))
 
(defn order? [order]
  (not (nil? (order-type order))))
   
(defn market?
  "determine if order is market order"
  [order]
  (let [type (order-type order)]
    (or (= type :sell) (= type :buy))))
 
(defn entry?
  "determine if order is entry order"
  [order]
  (let [type (order-type order)]
    (and type (not (or (= type :sell) (= type :buy))))))

(defn delete! [{id :id}]
  (core/order-delete id))

(defn close!  
  ([o] (close! o 0))
  ([{:keys [price lots slip id] :as order} new-lots]
     (is (and (string? id) (and price (pos? price))
	      (and (number? lots) (>= lots 0))))
     (is (>= (- lots new-lots) 0))
     (when (> (- lots new-lots) 0)
       (iff (core/order-close id (- lots new-lots) price slip :blue)
	    (merge order {:lots new-lots})
	    it))))
 
(defn modify!
  "modify sl and tp"
  [order {:keys [sl tp price]}]
  (let [sl (or sl (:sl order))
	tp (or tp (:tp order))
	price (or price (:price order))]
    (is (and sl tp price
	     (pos? sl) (pos? tp) (pos? price)))
    (core/order-modify (:id order) price sl tp)))

;;TOOD: how do we get map with defaults?
(defn- verify-order [{:keys [slip symbol type price tp sl lots]
		      :or {slip 3 sl 0 tp 0}}]
  (is (and (number? slip) (> slip 0) (integer? slip))
      "invalid order slip %s" slip)
  (is (and (keyword? type) (number? lots)
	   (number? tp) (number? sl) (number? price))) 
  (is (and (string? symbol) (> lots 0)
	   (>= tp 0) (>= sl 0) (>= price 0)))  
  (cond
    (or (= type :sell) (= type :sell-limit) (= type :sell-stop))
    (is (or (and (zero? sl) (zero? tp))
	    (and (zero? sl) tp (< tp price))
	    (and (zero? tp) sl (> sl price))
	    (and (< tp sl) (< tp price) (> sl price)))
	"invalid %s order with sl/tp %s/%s with price of %s" type sl tp price)
    (or (= type :buy) (= type :buy-limit) (= type :buy-stop))
    (is (or (and (zero? sl) (zero? tp))
	    (and (zero? sl) tp (> tp price))
	    (and (zero? tp) sl (< sl price))
	    (and (> tp sl) (> tp price) (< sl price)))
	"invalid %s order with sl/tp %s/%s with price of %s" type sl tp price)
    true (throwf "invalid %s order with sl/tp %s/%s with price of %s"
		 type sl tp price))) 

;;TODO: change to make reliable and to work for ECN brokers and such
(defn order! [{:keys [symbol type price tp sl lots slip]
	       :as order :or {slip 3 sl 0 tp 0}}]
  (verify-order order)
  (iff (core/order-send symbol type lots price sl tp slip)
       (merge {:sl sl :tp tp :slip slip} (merge order {:id it }))
       it))

(comment
  (defn order! [{:keys [symbol type price tp sl lots slip]
		 :as order :or {slip 3 sl 0 tp 0}}]
    (verify-order order) 
    (iff-let [id (core/order-send symbol type lots price)]
	     (do 
	       (if (or (and sl (not (zero? sl)))
		       (and tp (not (zero? tp))))
		 (iff (core/order-modify id price sl tp)
		      (merge order {:id id :slip slip :tp tp :sl sl})
		      (merge order {:id id :e it :sl 0 :tp 0}))
		 (merge order {:id id :sl 0 :tp 0})))
	     id))) 
 
(defn- immigrate [& syms]
  (let [core-ns (find-ns 'forex.module.account.core)
	publics (ns-publics 'forex.module.account.core)]
    (on [s syms]
      (let [sym  (symbol (camel-to-dash s))]
	(intern *ns* sym (var-get (intern core-ns sym)))))))

 
;;account common


(defn- sym [a] (symbol (camel-to-dash a)))
(defmacro- single [name] `(defn ~(sym name) [] (receive! ~name)))
(defmacro- double-single [name] `(defn ~(sym name) [] (receive-double! ~name)))
(defmacro- singles [& names] `(do ~@(map (fn [a] `(single ~a)) names)))
(defmacro- double-singles [& names] `(do ~@(map (fn [a] `(double-single ~a)) names)))

;;none of the below singles or double-singles should throw a mql error - therefore, it is a bug if they do
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
;;

(defmacro- define-market-info [& args]
  `(do ~@(map (fn [[name num]]
		`(defn ~(symbolicate "mode-" name) [symbol#]
		   (core/market-info symbol# ~num)))
	      (group args))))

(define-market-info
  low 1
  high 2
  time 5
  bid 9
  ask 10
  point 11
  digits 12
  spread 13
  stoplevel 14
  lotsize 15
  tick-value 16
  tick-size 17
  swaplong 18
  swapshort 19
  starting 20
  expiration 21
  trade-allowed 22
  minilot 23
  lotstep 24
  maxlot 25
  swaptype 26
  profitcalcmode 27
  margincalcmode 28
  margininit 29
  marginmaintenance 30
  marginhedged 31
  marginrequired 32
  freezelevel 33)

(defn demo? []
  (if (re-find #"(?i)demo" (account-server))
    true
    false))
