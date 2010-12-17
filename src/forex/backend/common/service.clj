;;forex.backend.common.service - getting streams and updating streams in a context from a backend

(ns forex.backend.common.service
  (:use emacs forex.backend.common.core 
        utils.general utils.fiber.spawn
	forex.util.general forex.util.log)
  (:require [forex.backend.common.core :as core])
  (:import (indicators.collection ForexStream))) 

(def- get-stream*
  (mem (fn [symbol timeframe] (pr "HI")
	 (let [core-stream (core/get-price-stream core/*backend*
						  symbol timeframe)	   
	       ret  (list (ForexStream. core-stream) core-stream)]
	   (swap! core/*streams* assoc (str symbol " " timeframe) ret)
	   ret))
       (naive-var-local-cache-strategy core/*streams-cache*)))
 
(defn get-stream [symbol timeframe] 
  (first (get-stream* symbol timeframe)))
 
(defn- update-all-price-streams [] 
  (with-read-lock core/*main-stream-lock*
    (with-write-lock core/*stream-lock*
      (on [[local main]  (vals @core/*streams*)] 
	(.update local main))))) 
  
(defn- update-all-indicators []
  (with-write-lock core/*stream-lock*
    (on [ind (vals @core/*indicators*)]
      (.ex ind)))) 

(defn refresh-rates []
  (with-read-lock core/*main-stream-lock*
    (with-write-lock core/*stream-lock*
      (update-all-price-streams)
      (update-all-indicators))))
 
(defmacro context [& body]
  `(binding [core/*indicators* (atom {})
	     core/*indicators-cache* (atom {})
	     core/*streams* (atom {})
	     core/*streams-cache* (atom {})
	     core/*stream-lock*
	     (java.util.concurrent.locks.ReentrantReadWriteLock.)]
     ~@body))


;;TODO: synchronize this with the major, global price stream. In other words, if i update right before the global one updates, well, i will be behind 1 tick. We could just of course speed up the global one, but synchronization is nicer. This is only needed for scalping.
;;TODO: SCALPING event driven, i.e. mql sends updates, instead of us polling? possible?
(defvar  service-global-refresh-poll-interval 1.01
  "refresh the global price stream every x seconds")

(comment
  (defn spawn-global-refresh-rates-service []
    {:pid (debugging "Global Refresh Rates:"
		     (spawn-log
		      #(loop []
			 (sleep service-global-refresh-poll-interval)
			 (refresh-rates)
			 (when (recv-if "stop" nil ? true)
			   (recur)))))}))

