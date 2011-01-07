
(ns forex-user
  (:use forex.util.general utils.general forex.util.log)
  (:use forex.module.error.common
        forex.module.ea.common
        forex.module.indicator.common
        forex.module.account.common)
  (:require [forex.backend.mql.socket-service :as backend]
            [clj-time.core :as t]))

;;utils
(defn pip-price
  ([] (pip-price (env :symbol)))
  ([symbol] (mode-tickvalue symbol)))
;;TODO: mql err on point? no way! we should throw an error
(defn point
  ([] (point (env :symbol)))
  ([symbol]
     (* 10 (mode-point symbol))))
(defn pips
  ([price] (pips price (env :symbol)))
  ([price symbol]
     (/ price (point symbol)))) 
(defn price-of
  ([val] (price-of val (env :symbol)))
  ([val symbol]
     (* (pip-price symbol) (pips val))))
(defn exit []
  (throwf "stopping ea"))
;;

(defn timeout-ea [order timeout self]
  (cond
   (not (open? order))
   (do (warn "order is closed ... exiting") (exit))
   (t/after? (t/now) timeout)
   (do
     (iff (delete! order) 
          (warn "entry order timed out ...")
          (warn "order is now a market order!"))
     (exit)) 
   (market? order)
   (do
     (warn "order is now a market order")
     (exit))))

(defn timeout [order]
  (partial timeout-ea order (t/plus (t/now) (t/hours 12))))

(defn order-it [percent {:keys [type symbol price sl tp1 tp2]}]
  (wenv (:symbol symbol)
        (let [two-percent (* (/ percent 100) (account-balance))
              lots (int (/ two-percent (price-of (Math/abs (- sl price)))))]
          (condp = lots 
              0 (warn "cannot make order with risk")
              (iff (order! {:type type :symbol symbol :price price :sl sl
                            :tp (if (= lots 1) tp1 tp1)
                            :lots (* (mode-minlot) lots)}) 
                   {:ea (run-ea {:name "timeout"
                                 :start (timeout it)})
                    :order it}
                   it)))))

(defn do-it []
  (def ea
    (order-it 3 {:type :sell-stop :symbol "USDCAD" :price 0.995 :sl 1.0028
                 :tp1 0.9924 :tp2 0.9898})))
