(ns forex.backend.common.mql
  (:require [forex.backend.mql.socket_service :as s]
	    [forex.backend.mql.price_stream_service :as p])
  (:use forex.backend.common.core   
	utils.general utils.fiber.spawn
	forex.util.general))
 
(defrecord+ mql [[socket-service (ref nil)] [price-service (ref nil)]] new-mql)
 
;;TODO: ability to start multiple mql services?
(def amount (atom 0))
;;TODO: what if one of the services dies!? OH WELL (for now until we get more like erlang in utils.fiber.spawn)

(extend-type mql
  PBackend
  (alive? [this]
	  (let [price @(:price-service this)]
	    (and price
		 (s/alive? (env :socket))
		 (pid? (:pid price)))))
  (get-price-stream [this symbol timeframe]
		    (is (alive? this) "mql service isnt alive!")
		    (p/get-price-stream symbol timeframe))
  (start [this params]
	 (is (= @amount 0)
	     "number of mql services is limited to only one at this time!")
	 (let [socket-service (s/start-mql)
	       price-service (do (sleep 2) (p/spawn-price-stream-service)
				 )]
	   (dosync (ref-set (:socket-service this) socket-service)
		   (ref-set (:price-service this) price-service)
		   )
	   (swap! amount inc)))
  (stop [this params]
	(is (alive? this) "mql service isn't alive!")
	(swap! amount dec)
	(let [price-service @(:price-service this)]
	  (dosync (ref-set (:socket-service this) nil)
		  (ref-set (:price-service this) nil))
	  ;;TODO: we really need a timeout on this!
	  ;;oh well, i want to get something working
	  (p/stop-price-stream-service price-service)
	  (sleep 2)
	  (s/stop-mql)))) 
 
 


