
;;TODO: unit test everything
;;TODO: create a new atom-hash on merging, etc????????
(clojure.core/use 'nstools.ns)
(ns+ forex.module.account
     (:clone clj.core)
     (:require forex.backend.mql.socket-service)
    ;; (:import forex.backend.mql.socket-service.MqlError)
     (:use forex.util.core
	   forex.util.emacs  
	   forex.util.general forex.util.log
	   forex.module.indicator
	   forex.module.error forex.module.account.utils)
 
     (:require  [forex.module.account.core :as core]))

(defvar *account-warn-on-mql-error* true)
 
(defn- account-warn
  ([err order] (account-warn err order ""))
  ([err order msg & args]
     (if *account-warn-on-mql-error* 
       (do (warn "MQL error %s on order %s - %s" (:e err)
                 order (apply format msg args)) err)
       err)))
(defmacro- with-order [order & body]
  `(let [o# ~order]
     (if-not (= (:lots o#) 0)
       (do ~@body)
       (do (warn "attempting to change order %s with zero lots" o#)
           o#)))) 

(defn- >? [a] (and (number? a) (>= a 0)))

(comment
  (defmacro- default [& body] 
    `(binding [*default* @~#'err/*er*]
       ~@body)))

;;TODO: do we need all of these assertions?
(def- value-to-order-type
  {0 :buy 1 :sell 2 :buy-limit
   3 :sell-limit 4 :buy-stop
   5 :sell-stop}) 

(defprotocol POrder
  (order-close-time [this])
  (order-type [this])
  (delete! [this] )
  (close! [this] [this new-lots])
  (modify! [this sl-tp-map])
  (order! [this])
  (open? [this]) (close? [this])
  (order? [this]) 
  (market? [this]) (entry? [this]))

(defn- order-close-time* [{id :id}]
  {:pre [(string? (first id))]}
  ;;we dont need to know mql4 error codes for order close time
  (aif (core/order-close-time id) it -1)) ;;TODO: return 0 instead?
(defn- order-type*  [{id :id}] 
  {:pre [(string? (first id))]}
  (aif (core/order-type id)
       (value-to-order-type (int it))))
(defn- delete!* [{id :id lots :lots :as o}]
  (with-order o
    (if-not (= lots 0)
      (aif (core/order-delete id) (merge o {:lots 0}) it)
      o)))

;;TOOD: make more robust? does metatrader always append modified trade? is it thread safe?
(defn- close!*
  ([o] (close! o 0))
  ([{:keys [price lots slip id symbol] :as order} new-lots]
     (with-order order
       (is? [(string? (first id)) (string? symbol) 
             [price (pos? price)]
             [(number? lots) (>= lots 0)]])
       (is? (>= (- lots new-lots) 0))
       (if-not (= new-lots lots)
         (aif (core/order-close id (- lots new-lots)
                                (if (sell? order)
                                  (ask symbol)
                                  (bid symbol))
                                slip :blue)
              (merge order {:lots new-lots 
			    :id (cond 
				 (= new-lots 0) id 
				 (= it false)
				 (do (severe "dropping a partial close id for order %s"
					     (merge order {:lots new-lots}))
				     id) 
				 true  (concat (list it) id))})
              (account-warn it order "invalid new lots %s" new-lots))
         order)))) 

;;TODO: normalize so we dont get mql error 1?
;;TODO: act as a regular modify if we dont pass in sl,tp,or price 
(defn- modify!* [order {:keys [sl tp] :as mod}]
  (with-order order
    (let [sl (or sl (:sl order))
          tp (or tp (:tp order)) 
          price (:price order)] 
      (if (or (number? sl) (number? tp))
        (do  
          (is? [sl tp price (>? sl) (>? tp) (pos? price)]) 
          (if-not (and (= sl (:sl order))
                       (= tp (:tp order)))
            (aif (core/order-modify (:id order) (:price order) sl tp)
                 (merge order (merge mod {:sl sl :tp tp}))
                 (if (= (:e it) 1)
                   (merge order mod)
                   (account-warn it order "sl %s tp %s" sl tp)))
            (merge order mod)))
        (merge order mod)))))   

;;TOODOs: how do we get map with defaults?
(defn- verify-order [{:keys [slip symbol type price tp sl lots]
                      :or {slip 3 sl 0 tp 0}}]
  (is? [(number? slip) (> slip 0) (integer? slip)]
       "invalid order slip %s" slip)
  (is? [ (keyword? type) (number? lots)
         (number? tp) (number? sl) (number? price)]) 
  (is? [ (string? symbol) (> lots 0)
         (>= tp 0) (>= sl 0) (>= price 0)])  
  (cond
   (or (= type :sell) (= type :sell-limit) (= type :sell-stop))
   (is? (or (and (zero? sl) (zero? tp))
            (and (zero? sl) tp (< tp price))
            (and (zero? tp) sl (> sl price))
            (and (< tp sl) (< tp price) (> sl price)))
        "invalid %s order with sl/tp %s/%s with price of %s" type sl tp price)
   (or (= type :buy) (= type :buy-limit) (= type :buy-stop))
   (is? (or (and (zero? sl) (zero? tp))
            (and (zero? sl) tp (> tp price))
            (and (zero? tp) sl (< sl price))
            (and (> tp sl) (> tp price) (< sl price)))
        "invalid %s order with sl/tp %s/%s with price of %s" type sl tp price)
   true (throwf "invalid %s order with sl/tp %s/%s with price of %s"
                type sl tp price))) 
;;TODO: change to make reliable and to work for ECN brokers and such
;;see http://forum.mql4.com/36608
;;TODO: what happens if second modify fails? how can programmer find this out?

(defn- order!* [{:keys [symbol type price tp sl lots slip]
                 :as order
                 :or { symbol (env :symbol) sl 0 tp 0 slip 3}}]
  (let [price (or price (if (sell? order) (bid symbol) (ask symbol)))
        order (merge 
               {:symbol symbol
                :slip slip}
               (merge order {:sl 0 :tp 0 :id "" :price price}))]
    (with-order order    
      (verify-order order)
      (aif (core/order-send symbol type lots price 0 0 slip)
           (let [result
		 (let [o (merge order {:id [it]})]
		   ;;now, use modify to change sl and tp
		   (aif (modify!* o {:sl sl :tp tp})
			(merge o {:sl sl :tp tp})
			o))
		 spread (spread)]
	     (merge result {:spread spread :break (o+ result price (pip spread))})) 
           (account-warn it order))))) 

(defn- open?* [order] 
  (= (order-close-time order) 0))
(defn- close?* [order]
  (not (open? order)))
(defn- order?* [order]
  (not (nil? (order-type order))))   
(defn- market?*
  "determine if order is market order"
  [order]
  (let [type (order-type order)]
    (or (= type :sell) (= type :buy)))) 
(defn- entry?*
  "determine if order is entry order"
  [order]
  (let [type (order-type order)]
    (and type (not (or (= type :sell) (= type :buy))))))

(extend clojure.lang.IPersistentMap
  POrder {:order-close-time order-close-time*
          :order-type order-type*
          :delete! delete!*
          :close! close!*
          :modify! modify!*
          :order! order!*

          :open? open?*
          :close? close?*
          :order? order?*
          :market? market?*
          :entry? entry?*})

(extend-type forex.backend.mql.error.MqlError
  POrder
  (order! [this] this)
  (delete! [this] this)
  (close!
   ([this] this)
   ([this _] this))
  (modify! [this] this))

(extend-type forex.util.core.AtomHash ;;clojure.lang.Atom
  POrder
  (order-close-time [this] (order-close-time @this))
  (order-type [this] (order-type @this))
  (order! [this] (aif (order! @this)
		      (do (reset! (.val this) it) this)
		      it))
  (delete! [this] (aif (delete! @this) (do (reset! (.val this) it) this) it))  
  (close!
   ([this new-amount] (aif (close! @this new-amount) (do (reset! (.val this) it) this) it))
   ([this] (close! this 0)))
  (modify! [this sl-tp] (aif (modify! @this sl-tp) (do (reset! (.val this) it) this) it))
  (open? [this] (open? @this))
  (close? [this] (not (open? @this)))
  (order? [this] (order? @this))
  (market? [this] (market? @this))
  (entry? [this] (entry? @this)))


