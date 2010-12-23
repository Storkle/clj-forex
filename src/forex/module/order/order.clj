;; module.order.order - order object for creating orders

;;NOTICE: dont share orders between threads, well you could use agents i suppose, but whats the purpose? 

(ns forex.module.order.order
  (:use utils.general))
;;TODO: we need a modular 'defrecord', in which we can have these defaults but extend them easily

;;TODO: doesnt quite work as it should (merge), oh well
(deftype order [current new]
  Object
  (toString [this] (str "<Order " @(.current this) " " @(.new this) ">"))
  clojure.lang.ILookup
  (valAt [this key] (get @(.current this) key))
  (valAt [this key notfound] (get @(.current this) key notfound))
  clojure.lang.IPersistentMap
  (empty [this] (throw (UnsupportedOperationException. (str "Can't create empty: order4"))))
  (cons [this e] (cons @(.current this) e))
  (equiv [this gs] (or (identical? this gs)
		       (when (identical? (class this) (class gs))
			 (.equiv @(.current this) gs))))
  (containsKey [this k] (or (and (get @(.current this) k) true) false))
  (entryAt [this k] (get @(.current this) k))
  (seq [this] (seq @(.current this)))
  (assoc [this k g] (assoc @(.current this) k g))
  (without [this k] (.without @(.current this) k)))

(defmethod clojure.core/print-method order [o w]
  (.write w (.toString o)))

(defn verify-order [map]
  (let [lots (:lots map)
	sl (:sl map)
	tp (:tp map)
	type (:type map)]
    (is (and (number? lots) (or  (zero? lots) (pos? lots))))
    (is (and (number? sl) (or (zero? sl) (pos? sl))))
    (is (and (number? tp) (or (zero? tp) (pos? tp))))
    (is (or (= type "buy") (= type "sell") (= type "buy_stop") (= type "sell_stop") (= type "sell_limit") (= type "buy_limit")))))

(defn new-order [type map]
  (is (map? map) "invalid param: must be a map")
  (let [new-map (merge map {:type type :sl 0 :tp 0 :lots 0})]
    (verify-order new-map)
    (order. (ref new-map) (ref {}))))

(defn modify [o map]
  (dosync
   (alter
    (.new o)
    (fn [old]
      (let [map (merge old (dissoc map :type))
	    new-map (merge @(.current o) (apply dissoc map (clojure.set/difference (set (keys map)) (set (keys o)))))]
	(verify-order new-map)
	new-map)))))
 
