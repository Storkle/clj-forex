(ns forex.backend.mql
  (:require [forex.backend.mql.socket :as s]
	    [forex.backend.mql.service :as service])
  (:use utils.general utils.fiber.spawn forex.utils))
(defprotocol BACKEND
  "Backend Interface"
  (get-price-stream [this symbol timeframe])
  (alive? [this])
  (start [this params])
  (stop [this params]))

(defrecord+ mql [[socket-service (ref nil)] [price-service (ref nil)]] new-mql)

;;TODO: ability to start multiple mql services?
(defonce- amount (atom 0))
;;TODO: what if one of the services dies!? OH WELL (for now until we get more like erlang in utils.fiber.spawn)

(extend-type mql
  BACKEND
  (alive? [this]
	  (let [price @(:price-service this)]
	    (and price
		 (s/alive? (env :socket))
		 (pid? (:pid price)))))
  (get-price-stream [this symbol timeframe]
		    (is (alive? this) "mql service isnt alive!")
		    (service/get-price-stream symbol timeframe))
  (start [this params]
	 (is (= @amount 0)
	     "number of mql services is limited to only one at this time!")
	 (let [socket-service (s/start-mql)
	       price-service (do (sleep 2) (service/spawn-price-stream-service))]
	   (dosync (ref-set (:socket-service this) socket-service)
		   (ref-set (:price-service this) price-service))
	   (swap! amount inc)))
  (stop [this params]
	(is (alive? this) "mql service isn't alive!")
	(let [socket-service (:socket-service this)
	      price-service (:price-service this)]
	  ;;(dosync (ref-set socket-service nil) (ref-set price-service nil))
	  ;;TODO: we really need a timeout on this! oh well, i want to get something working
	    (service/stop-price-stream-service @price-service) (sleep 2)
	     (s/stop-mql) 
	)
	(swap! amount dec)))
 



