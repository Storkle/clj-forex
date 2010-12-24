(in-ns 'forex_user)
;;timeout - cancel an entry order after a certain time period
(require '[clj-time.core :as t])
(use 'forex.util.log)

(defn close-entry-order [o]
  (if (and (entry? o)
	   (open? o))
    (do (warn "Closing order ...")
	(delete! o)
	(warn "done"))
    (do (warn "Not closing order"))))
 
(defn recv-stop [timeout]
  (recv- (? timeout) "stop" nil ? true))

(defn order-timeout [o timeout]
  (is (entry? o))
  {:pid
   (debugging
    (str (env :symbol) "/" (env :timeframe))
    (spawn #(when (recv-stop (* timeout 3600 1000))
	      (close-entry-order o))))})
 
(defn ex
  "create a test order and then create a timeout in another thread. Perhaps we should put this in a global poll loop, that would be better?"
  [timeout]
  (defonce tt
    (order! {:symbol "EURUSD"
	     :type :sell-stop
	     :lots 0.1
	     :tp 1.30872 :sl  1.31167
	     :price 1.31033}))
  (def pid (order-timeout tt timeout)))
(defn ex? [] (pid? (:pid pid)))
(defn stop! [] (! (:pid pid) "stop"))