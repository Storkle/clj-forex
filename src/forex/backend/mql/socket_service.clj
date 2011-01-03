;;forex.backend.mql.socket-service: provide background sockets which allow us to connect with metatrader. Provides functions to interact with the background socket
    
(ns forex.backend.mql.socket-service   
  (:require 
   [org.zeromq.clojure :as z]
   [utils.fiber.mbox :as m]
   [clojure.contrib.logging :as l])
  (:use
   emacs 
   forex.util.general forex.util.zmq forex.util.log
   forex.util.spawn utils.general))
 
;;TODO: 3ms or so per request, a little slow...
;;also, unfortunately, if we add more servers, speed doesn't increase linearly. so the bottleneck is in the clojure code ... a better designed socket service should really be made.
;; in addition, if servers drop out, we will be waiting forever for them. this is bad.

(defvar mql-socket-recv-address "tcp://127.0.0.1:3000")
(defvar mql-socket-send-address "tcp://127.0.0.1:3005")
(defvar mql-socket-pub-address "tcp://127.0.0.1:3010")

;;utils
(defonce- *msg-id* (atom 0))
(defn- msg-id []
  (str (swap! *msg-id* inc)))
(defmacro catch-unexpected [& body]
  `(try (do ~@body)
	(catch Exception e# (.printStackTrace e#) (warn e#))))

;;socket service
;;TODO: send id then message
(defn- mql-recv [ids msg] 
  (catch-unexpected   
   (let [key (first msg)
	 msg-ask (@ids key)]
     (if-not (satisfies? PWait msg-ask)
       (warn "Ignoring invalid msg: %s" msg)
       (do 
	 (give msg-ask (second msg))
	 (swap! ids dissoc key))))))
   
(defn- socket-service-match [events ids send receive]
  (match  
   (first events)
   [local "STOP"] (do (info "closing ...") "stop")  
   [local ["REQUEST" ?msg ?askin]]  
   (if-not (satisfies?  PWait askin)
     (warn "Ignoring invalid REQUEST which does not contain a PWait argument %s %s"
	   msg askin) 
     (let [id (msg-id)  
	   result  (.snd send (str id " " msg) +noblock+)]
       (if-not result  
	 (do  
	   (warn "failed to queue request %s: are any metatrader scripts alive?"
		 msg)
	   (catch-unexpected
	    (give askin (Exception. "socket service down"))))
	 (swap! ids assoc id askin))))  
   [receive ?msg] (mql-recv ids msg) 
   ?msg (warn "Ignoring invalid message %s" msg)))
  
(defn spawn-mql-socket-service
  []  
  (debugging
   "MQL Socket Service: " 
   (let [ids (atom {})]
     {:pid 
      (spawn-log  
       #(with-open [send (doto (new-socket +push+)
			   (.bind mql-socket-send-address))
		    receive (doto (new-socket +pull+)
			      (.bind mql-socket-recv-address))]
	  (loop [events (event-seq [receive local])]
	    (when-not (= "stop" (socket-service-match events ids send receive))
	      (recur (rest events))))))}))) 
 
;;global socket service
(defonce- *s* (atom nil))
(defn alive? []
  (pid? (:pid @*s*))) 
(defn start []
  (if (alive?)
    (warn "mql socket is already alive!")
    (reset! *s* (spawn-mql-socket-service))))
(defn stop []
  (if (alive?)
    (! (:pid @*s*) "STOP")
    (warn "mql socket service is already stopped")))
      
;;interact with mql 
(defn request [askin msg]
  (io!
   (if (pid? (:pid @*s*)) 
     (! (:pid @*s*) ["REQUEST" msg askin])
     (throwf "mql socket service is not alive"))))
         
(defn receive
  ([msg] (receive msg nil))
  ([msg timeout]
     (let [askin (beg)]
       (request askin msg)
       (let [result (if (wait-for askin timeout) @askin)]
	 (cond
	  (instance? Exception result) (throw result)
	  result result
	  true (throwf "invalid result received %s" result)))))) 

 
 