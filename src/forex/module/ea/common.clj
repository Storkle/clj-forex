(ns forex.module.ea.common
  (:use utils.general forex.util.general
	utils.fiber.spawn clojure.contrib.core)
  (:use 
   forex.util.log
   forex.module.error.common
   forex.module.indicator.common
   forex.module.account.common))

(defonce *ea* (atom {})) 
(defn bar-id [] (open))

;;(add-ea "timeout" {:symbol "EURUSD" :timeframe 60} timeout-ea)

(comment
  (defn timeout-ea []
   (pformat "close is %s%n" (close))))
(defn stop-all-eas []
  (on [lst (vals @*ea*)]
      (on [ea (vals lst)]
	  (try
	    (when ea (! (:pid ea) "stop"))
	    (catch Exception e
	      (pr ea)
	      (swap! *ea* dissoc-in [(:type ea) (:pid ea)])))))
  true)
  
(defn run-ea [symbol timeframe name thunk]
  (try
    (wenv (:symbol symbol :timeframe timeframe)
	  (loop [prev-close nil]
	    (sleep 1)
	    (when-not (match (? 0) "stop" true)
	      (let [new-close (close)]
		(when-not (= new-close prev-close)
		  (thunk))
		(recur new-close))))) 
    (catch Exception e 
      (severe "Deinitializing ea %s: %s" name e)
      (.printStackTrace e))
    (finally (warn "Stopping ea: %s" name))))
		   
(defn run-ea [type {symbol :symbol timeframe :timeframe} function]
  (let [prev (get @*ea* type)
	pid  (spawn-log
	      (partial
	       run-ea symbol timeframe
	       (format "EA %s: %s_%s" type symbol timeframe)
	       function))
	ea-id {:id (gensym) :type type :pid pid
	       :symbol symbol :function function
	       :timeframe timeframe}]
    (swap! *ea* assoc-in [type pid] ea-id)
    ea-id))
  
(comment
  (defn order-it []
   (run-ea "timeout" {:symbol "EURUSD" :timeframe 240}
	   (partial timeout-ea order timeout)))

  (defn timeout-ea [order timeout]
    (cond
     (and (>= time timeout) (entry? order))
     (do
       (delete! order) 
       (warn "entry order timed out ...")
       (stop)) 
     (market? order)
     (do
       (warn "order is now a market order")
       (stop)))))
