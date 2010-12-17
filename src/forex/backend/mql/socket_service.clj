;;forex.backend.mql.socket_service: provide background sockets which allow us to connect with metatrader. Provides functions to interact with the background sockets

;;TODO chage socket type to appropriate type?
;;TODO: general zeromq server types? and fix the type we use, it is wrong
    
(ns forex.backend.mql.socket_service  
  (:require 
   [org.zeromq.clojure :as z]
   [utils.fiber.mbox :as m]
   [clojure.contrib.logging :as l])
  (:use emacs
   forex.util.zmq forex.util.general forex.util.log
   utils.fiber.spawn utils.general))

;;TODO: ports as user defined variable??

(defn mql-recv [mailbox msg]
  (let [data (split  msg #" +")]
    (m/! mailbox (first data) (rest data)))) 

(defn spawn-mql-recv-service [{host :host port :port timeout :timeout}]
  (let [mbox (m/new-mbox)] 
    {:pid (spawn-log
	   #(let [receive (socket-new {:host host :port port} z/+sub+)
		  poller (socket-new-poller receive)]
	      (try
		(do 
		  (.subscribe receive (.getBytes ""))		
		  (z/connect receive (str "tcp://" host ":" port))
		  (info "starting receive service")
		  (loop []
		    (when-not (recv-if "stop"
				       (do
					 (info "stopping receive service")
					 true))
		      (when-let [data (socket-receive receive poller timeout)]
			(mql-recv mbox data)) 
		      (recur))))
		(catch Exception e (severe "receive service error - %s" e))
		(finally (.close receive))))) 
     :mbox mbox}))


(defn spawn-mql-send-service [args]
  {:pid (spawn-log
	 #(let [send (socket-new args z/+pub+)]
	    (try
	      (do 
		(z/bind send (str "tcp://" (:host args) ":" (:port args)))
		(info "starting send service")
		(loop []
		  (when-not
		      (recv
			"stop"
			(do (info "stopping send service") true)
			["send" ?data]
			(do (z/send- send data)
			    nil)) 
		    (recur))))
	      (catch Exception e (severe "send service error - %s" e))
	      (finally (.close send)))))})

(defn alive? [a]
  (let [{receive :receive send :send} a]
    (and (pid? (:pid receive))
	 (pid? (:pid send)))))

(defn start []
  (debugging "MQL Socket:"
   (let [id (gensym)]
     {:id id 
      :receive (spawn-mql-recv-service {:host "127.0.0.1" :port 2055})
      :send  (spawn-mql-send-service {:host "127.0.0.1" :port 2045})}))) ;;2045

(defn stop [server]
  (let [{receive :receive send :send} server]
    (if (pid? (:pid receive))
      (! (:pid receive) "stop")
      (warn "receive service is already stopped"))
    (if (pid? (:pid send))
      (! (:pid send) "stop")
      (warn "send service is already stopped"))))

;;interact with sockets

(defonce- index (atom 0))

(defn- send* [server msg] 
  (is (string? msg) "message must be a string!")
  (let [id (swap! index inc)]
    (! (:pid (:send server)) ["send" (.getBytes (str id " "  msg))])
    (str id))) 

(defn- receive*
  ([server id timeout] (m/? (:mbox (:receive server)) id timeout))
  ([server id] (receive* server id nil)))

(defn start-mql []
  (env! {:socket (start)}))

(defn stop-mql []
  (stop (env :socket))
  (env! {:socket nil}))
 
;;TODO: throw an error on timeout?
(defn receive
  ([msg timeout]
     (is (alive? (env :socket)) "mql socket isnt alive")
     (let [a (env :socket)
	   id (send* a msg)]
       (receive* a id timeout)))
  ([msg] (receive msg nil)))
