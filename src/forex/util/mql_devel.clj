
(clojure.core/use 'nstools.ns)
(ns+ forex.util.mql-devel
     (:clone clj.core)
     (:use forex.util.general))


(defmacro cond-out [& args]
  (apply str (map (fn [a]
                    (let [name (str a)]
                      (format " else if (command==\"%s\") {\n\t   ret = process_%s(request);\n\t}" name name)))
                  args)))

(cond-out
 AccountBalance
 AccountCredit
 AccountCompany
 AccountCurrency
 AccountEquity
 AccountFreeMargin
 AccountLeverage
 AccountMargin
 AccountName
 AccountNumber
 AccountServer
 AccountProfit
 OrderLots
 OrderDelete
 OrderCloseTime
 OrderType
 OrdersTotal
 OrderSend
 MarketInfo
 OrderClose
 OrderModify)

