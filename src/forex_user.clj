;;forex_user.clj - user customization file that should be run on startup

;;TODO: if we exit metatrader without stopping backend, the address wont be torn down:( so we would have to wait for timeout
;; and then the price stream service cant be stopped (we dont have a timeout for accessing socket)
;;TODO: if we let streams not update, then when we update next one, it will be up to date - we dont really want this, so its a bug
;;TODO: clear/update price stream hashing and main stuff after stop backend
 
(comment

(ns forex_user 
  (:use utils.general emacs utils.fiber.spawn
	forex.util.general
	forex.module.indicator.common forex.module.account.common)
  (:require [forex.backend.common :as backend]
	    [forex.backend.mql.price_stream_service :as price]))

(env! {:symbol "EURUSD" :timeframe 1}) ;;default to eurusd, 1 minute timeframe 
;;constantly update global price context
;;TODO: run all hooks no matter what??? 
(add-to-list #'price/mql-price-stream-update-hook backend/refresh-rates)
;;set update interval in seconds of global price: dont set it too low here
;;now in metatrader 5, we can take advantage of the 'on price changed' event, maybe???
(setq price/mql-poll-interval 0.5) 


)

