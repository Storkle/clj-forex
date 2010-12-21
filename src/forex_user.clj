;;forex_user.clj - user customization file that should be run on startup

(ns forex_user 
  (:use utils.general emacs
	forex.indicator.common forex.util.general)
  (:require [forex.backend.common :as backend]))
(require '[forex.backend.mql.price_stream_service :as price])

(env! {:symbol "EURUSD" :timeframe 1}) ;;default to eurusd, 1 minute timeframe
;;constantly update global price context
(add-to-list #'price/mql-price-stream-update-hook backend/refresh-rates)
;;set update interval in seconds of global price: dont set it too low here
;;now in metatrader 5, we can take advantage of the 'on price changed' event, maybe???
(setq price/mql-poll-interval 0.5) 
