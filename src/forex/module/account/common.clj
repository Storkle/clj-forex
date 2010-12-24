;;forex.module.order.common - api for creating/modify orders
;;TODO: what happends when a metatrader error is thrown?
(ns forex.module.account.common
  (:use utils.general emacs 
	forex.util.general
	forex.backend.mql.socket_service)
  (:require  [forex.module.account.core :as core])
  (:require [forex.backend.common :as backend]))
    
(comment
  (defn g []
    (def o (order! {:symbol "EURUSD"
		    :price 1.31133	  
		    :type :sell-stop		 
		    :lots 0.1}))))
  
(def- value-to-order-type
  {0 :buy 1 :sell 2 :buy-limit
   3 :sell-limit 4 :buy-stop
   5 :sell-stop})

(defn order-close-time [{id :id}]
  (is (string? id))
  (core/order-close-time id))

(defn open? [order]
  (= (order-close-time order) 0))
 
(defn order-type
  "type of order, even if it is already closed"
  [o]
  (is (string? (:id o)))
  (value-to-order-type (int (core/order-type (:id o)))))

(defn market?
  "determine if order is market order"
  [order]
  (let [type (order-type order)]
    (or (= type :sell) (= type :buy))))
(defn entry? [order]
  (not (market? order)))

;;BUG: somehow, metatrader sometimes freezes when making an order. hmmm...... not good!
;;or wait, maybe this is the other bug?
(comment
  (defmulti close!
    (fn [{type :type} & args]
      (cond 
	(or (= type :sell-limit) (= type :buy-limit)
	    (= type :sell-stop) (= type :buy-stop))
	:entry
	(or (= type :sell) (= type :buy)) 
	:market
	true
	:default))))

(defn delete! [{id :id :as order}]
  (core/order-delete id)
  order)

(comment
  (defmethod close! :entry [{id :id :as order}] 
    (core/order-delete id)
    order))
  
(defn close! 
  ([o] (close! o 0))
  ([{:keys [price lots slip id] :as order} new-lots]
     (is (and (string? id) (pos? price) (and (number? lots) (>= lots 0))))
     (is (>= (- lots new-lots) 0))
     (when (> (- lots new-lots) 0)
       (core/order-close id (- lots new-lots) price slip :blue))
     (merge order {:lots new-lots})))
 
(defn modify!
  "modify sl and tp"
  [{:keys [id]} {:keys [sl tp price]}]
  (is (and (pos? sl) (pos? tp) (pos? price) (string? price)))
  (core/order-modify id price sl tp))

;;TOOD: how do we get map with defaults?
(defn- verify-order [{:keys [slip symbol type price tp sl lots] :or {slip 3 sl 0 tp 0}}]
  (is (and (number? slip) (> slip 0) (integer? slip)) "invalid order slip %s" slip)
  (is (and (keyword? type) (number? lots) (number? tp) (number? sl) (number? price)))
  (is (and (string? symbol) (> lots 0) (>= tp 0) (>= sl 0) (>= price 0)))  
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
    true (throwf "invalid %s order with sl/tp %s/%s with price of %s" type sl tp price)))

(defn order! [{:keys [symbol type price tp sl lots slip] :as order :or {slip 3 sl 0 tp 0}}]
  (verify-order order)
  (let [id (core/order-send symbol type lots price)]
    (when (or (and sl (not (zero? sl)))
	      (and tp (not (zero? tp))))
      (core/order-modify id price sl tp))
    (merge order {:id id :slip slip :tp tp :sl sl})))
 
(defn- immigrate [& syms]
  (let [core-ns (find-ns 'forex.module.account.core)
	publics (ns-publics 'forex.module.account.core)]
    (on [s syms]
      (let [sym  (symbol (camel-to-dash s))]
	(intern *ns* sym (var-get (intern core-ns sym)))))))

;;account common
(immigrate 
 "AccountCurrency"
 "AccountCompany"
 "AccountServer" 
 "AccountName"
 "AccountNumber"

 "AccountCredit"
 "AccountBalance"
 "AccountEquity"
 "AccountFreeMargin"
 "AccountLeverage"
 "AccountMargin"
 "AccountProfit"
 "OrdersTotal")