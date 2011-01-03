;;forex.backend.common.service - getting streams and updating streams in a context from a backend

(ns forex.backend.common.service
  (:use emacs forex.backend.common.core 
        utils.general utils.fiber.spawn
	forex.util.general forex.util.log)
  (:require [forex.backend.common.core :as core])
  (:import (indicators ForexStream))) 

(comment

(def- get-stream*
  (mem (fn [symbol timeframe] 
	 (let [core-stream (core/get-price-stream core/*backend*
						  symbol timeframe)	   
	       ret  (list (ForexStream/create core-stream) core-stream)]
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

)
