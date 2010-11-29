;;TODO chage socket type to appropriate type
(ns forex.socket
  (:require
	    [forex.zmq :as z])
  ;(:import java.util.concurrent.locks.ReentrantLock)
  (:use forex.utils))
 
;;TODO: how to handle closing of server? 
(def #^{:private true} *id* (atom  0))
;;todo: receive timeout??
(defn receive [msg] 
  (let [i (if (> @*id* 100000000)
	    (! *id* 0)
	    (swap! *id* inc))]
    (z/pu (env :server) (str i " " msg)) 
    (z/ge (env :client) i)))
  
(defn- receive-loop [data]
  (try
   (let [dat (split data #" +")]
     [(Integer/parseInt (first dat))
      (rest dat)])
   (catch Exception e
     (log e)
     [-1 e])))
 
;;TODO: disconnecting when not connecting?
;;TODO: mql is simply another socket,right? so use interface?
;; and how to make thread safe with two sockets?
(defn mql-connect []
  (if (or (env :client) (env :server))
    (throwf "already connected!") 
    (let [server (z/new-server {:host "127.0.0.1" :port 2045} z/+pub+)
	  client (z/new-client {:host "127.0.0.1" :port 2055 :timeout 100}
			       z/+sub+)]
      (try
	(do 	 
	  (.subscribe (:socket client) (.getBytes ""))
	  (z/connect server nil)
	  (z/connect client receive-loop)
	  (env! {:client client :server server})
	  {:client client :server server})
	(catch Exception e
	  (z/finalize server) (z/finalize client)
	  (env! {:server nil :client nil})
	  (throw e))))))
 
(defn mql-alive? []
  (and (z/alive? (env :client)) (z/alive? (env :server))))
(defn mql-disconnect []
  ;;terminate context
  (let [client (env :client) server (env :server)]
    (when (or client server)
      (env! {:client nil :server nil})
      (z/finalize client) (z/finalize server))))


 
 





