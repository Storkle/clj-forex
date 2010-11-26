(ns forex.indicator
  (:refer-clojure :exclude (=))
  (:import (forex.indicator SMA EMA CCI ATR FantailVMA RSI))
  (:use forex.utils forex.binding))

;;TODO change hashcode of ForexStream!
				
(defonce- *mem* (atom {})) ;TODO: prevent evil printing of indicators
; below taken from http://kotka.de/blog/2010/03/memoize_done_right.html
(defn memoi
  ([f] (memoi f [{} identity (fn [mem args] mem) assoc]))
  ([f [init cached hit assoc]]
   (let [mem         *mem* 
         hit-or-assoc (fn [mem args]
                        (if (contains? (cached mem) args)
                          (hit mem args)
                          (assoc mem args (delay (apply f args)))))]
     (fn [a & args]
       (let [m (swap! mem hit-or-assoc args)]
         (-> (cached m) (get args) deref))))))


(defn _get-indicator [f ind-name symbol timeframe params]
  (let [ind (f symbol timeframe params)]
    (.update ind) ;TODO: this needs to lock indicator? write lock?
    ind))
(def get-indicator (memoi _get-indicator))
 
(defn substream [stream price]
  (cond
    (is price :close) (.Close stream)
    (is price :open) (.Open stream)
    (is price :high) (.High stream)
    (is price :low) (.Low stream)
    true price))

(defn env-stream []
  (let [stream (get-stream (env :symbol) (env :timeframe))]
    (env! {:stream stream})
    stream))
 
(defn sma
  ([period] (sma period 0))
  ([period index] (sma period index {}))
  ([period index {:keys [price stream] :or {stream (env-stream)
					    price :close}}]
     (let [params [stream (substream stream price) period]
	   symbol (env :symbol)
	   timeframe (env :timeframe)
	   indicator (get-indicator #(SMA. % %2 %3) 'sma symbol timeframe params )
	   get-it (fn [index] (.get indicator index))] 
       (if (is index nil)
	 get-it
	 (get-it index))))) 

 

