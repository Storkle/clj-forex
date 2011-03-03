
(clojure.core/use 'nstools.ns)

(ns+ forex.module.account.utils
     (:clone clj.core)
     (:use forex.util.core
	   forex.util.emacs  
	   forex.util.general
	   forex.module.error)
     (:require  [forex.module.account.core :as core]))

;;account common
(defn- sym [a] (symbol (camel-to-dash a)))
(defmacro- single [name] `(defn ~(sym name) [] (receive! ~name)))
(defmacro- singles [& names] `(do ~@(map (fn [a] `(single ~a)) names)))


;;none of the below singles or double-singles should throw a mql error - therefore, it is a bug if they do
(singles
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
(defn demo? [] (receive "IsDemo"))
(defn connected? [] (receive "IsConnected"))
(defn trade? [] (receive "IsTradeAllowed"))

;;

(defmacro- define-market-info [& args]
  `(do ~@(map (fn [[name num]]
                `(defn ~(symbolicate "mode-" name)
                   ([] (~(symbolicate "mode-" name) (env :symbol)))
                   ([symbol#]
                      (let [res# (core/market-info symbol# ~num)]
                        (if (e? res#)
                          (throwf "market-info error %s"
				  res#)
                          res#)))))
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
  tickvalue 16
  ticksize 17
  swaplong 18
  swapshort 19
  starting 20
  expiration 21
  trade-allowed 22
  minlot 23
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

(defn sell? [{type :type}]
  (or (= type :sell) (= type :sell-stop) (= type :sell-limit)))
(defn buy? [{type :type}]
  (or (= type :buy) (= type :buy-stop) (= type :buy-limit)))
(defn- to-big [num]
  (BigDecimal/valueOf (if (integer? num) (double num) num))) 
(defn lot 
  ([num] (lot num (env :symbol)))
  ([num symbol]
     (double (let [a (to-big num)  
		   b (to-big (mode-minlot symbol))]
	       (* b (.intValue (/ a b)))))))

(defn- assert-order [order] (is? [(or (sell? order) (buy? order))]))

(defn o-- [o & args]
  (assert-order o)
  (if (sell? o) (- (apply - args)) (apply - args)))

(defn o+ [o & args]
  (assert-order o) 
  (if (sell? o) (apply - args) (apply + args)))
(defn o- [o & args]
  (assert-order o)
  (if (sell? o) (apply + args) (apply - args)))

(defn omax [order & args]
  (assert-order order)
  (let [args (filter #(not (zero? %)) args)]
    (if (empty? args)
      (or (:sl order) 0) 
      (apply (if (buy? order) max min) args))))

(defn omin [order & args]
  (assert-order order)
  (let [args (filter #(not (zero? %)) args)]
    (if (empty? args)
      (or (:sl order) 0) 
      (apply (if (buy? order) min max)
             args))))


;;TODO: change for 4 digit broker
(defn digits []
  (mode-digits "EURUSD"))
 
;;TODO: check for valid symbol on all of this??
(defn spread
  ([] (spread (env :symbol)))
  ([symbol]
     (let [d (digits) spread (mode-spread symbol)]
       (cond
        (= d 5) (/ spread 10)
        (= d 4) spread))))

(defn point
  ([pt] (/ pt (point)))
  ([]
     (let [d (digits)]  
       (if (= d 5)
         (* 10 (mode-point))
         (mode-point)))))

(defn pip [pt] (* (point) pt)) 

