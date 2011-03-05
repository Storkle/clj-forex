;;forex.backend.mql.socket-service: provide background sockets which allow us to connect with metatrader. Provides functions to interact with the background socket

;;todo: bug with stopping all and then stopping again! so bug with stop..
(clojure.core/use 'nstools.ns)
(ns+ forex.backend.mql.socket-service
     (:clone clj.core)
     (:require
      clojure.contrib.core
      [forex.util.fiber.mbox :as m]
      [clojure.contrib.logging :as l]) 
     (:import (java.io DataInputStream ByteArrayInputStream))
     (:use
      forex.backend.mql.error
      forex.backend.mql.utils
      forex.util.emacs 
      forex.util.core forex.util.general
      forex.util.zmq forex.util.log
      forex.util.spawn))

(defvar mql-socket-recv-address "tcp://127.0.0.1:3010")
(defvar mql-socket-send-address "tcp://127.0.0.1:3005")

(defonce- *ids* (atom {}))
;;socket service
 
(defn invalid-msg? [msg]
  (if (clojure.contrib.core/seqable? msg)
    (if (empty? msg) true false)
    (not (string? msg))))

(defn mql-to-java [type info]
  (cond
   (= type "long") (Long/parseLong (String. info))
   (= type "boolean")
   (let [a (Integer/parseInt (String. info))]
     (condp = a
	 1 true
	 0 false
	 (Exception. (str "unkown boolean return result of " a))))
   (= type "double[]") (into-doubles info)
   (= type "double") (Double/parseDouble (String. info)) 
   (= type "int") (Integer/parseInt (String. info))
   (= type "string") (String. info) 
   (= type "error") (new-mql-error (parse-int (String. info)))
   (= type "global") (String. info)
   true (Exception. (str "Unkown mql type " type))))

(defn mql-parse-recv [msg]
  (let [[^bytes id & info] msg
	id (String. id)]
    ;;(println (format "id is %s info is %s" id info))
    (when id
      [id (doall (for [[type val] (group info)]
		   (mql-to-java (String. type) val)))])))

(defn mql-recv [msg] 
  (catch-unexpected
   (let [[id result] (mql-parse-recv msg)]
     (when id
       (let [msg-ask (get @*ids* id)] 
	 (if msg-ask
	   (do (deliver msg-ask result) (swap! *ids* dissoc id))
	   (warn "msg-ask corresponding to id %s is nil" id))))))) 

;;TODO: get rid of reflection warnings?
;;FIXME: snd crashes when not given a string?
(defn snd-multi [socket id msg]
  (when-not (invalid-msg? msg) 
    (when (.snd socket (str id) (bit-or +noblock+ +more+))
      (if (or (vector? msg) (list? msg)) 
	(loop [m msg]
	  (if (= 1 (count m)) 
	    (.snd socket (str (first m))  +noblock+)
	    (if (.snd socket (str (first m))
		      (bit-or +more+ +noblock+))
	      (recur (next m))
	      false))) 
	(.snd socket (str msg) +noblock+)))))  

;;TODO: make shorter
(defn- socket-service-match [events send receive]
  (let [event (first events)]
    (match
     event
     [local "STOP"] (do (info "closing ...") "STOP")  
     [local ["REQUEST" ?msg ?askin]]  
     (when-not (invalid-msg? msg)
       (let [id (msg-id)  
	     result (snd-multi send id msg)]
	 (if result
	   (when-not (nil? askin)
	     (swap! *ids* assoc id askin))
	   (when-not (nil? askin)  
	     (catch-unexpected
	      (deliver askin (list
			      (new-mql-error +error-clj-service-queue+)))))
	   )))  
     [receive ?msg] (mql-recv msg)   
     ?msg (warn "Ignoring invalid message %s" msg))))
 
;;TODO: weird bug when stopping everything with an ea.

(defn spawn-mql-socket-service
  []  
  (debugging
   "MQL Socket Service: " 
   {:pid 
    (spawn-log  
     #(with-open [send (doto (new-socket +push+)
                         (.bind mql-socket-send-address))
                  receive (doto (new-socket +pull+)
                            (.bind mql-socket-recv-address))] 
        (loop [events (event-seq [receive local])]
          (when-not (= "STOP" (socket-service-match events send receive))
            (recur (rest events)))))
     "MQL Socket Service")})) 

;;global socket service
(defonce- *s* (atom nil)) 
(defn alive? []
  (pid? (:pid @*s*))) 
(defn start []
  (if (alive?)
    (warn "mql socket is already alive!")
    (do (reset! *ids* {}) (reset! *s* (spawn-mql-socket-service)))))
(defn stop []
  (if (alive?)
    (! (:pid @*s*) "STOP")
    (warn "mql socket service is already stopped")))
 
;;interact with mql
;;TODO: if mql isnt alive and we retry/????
(defn request
  ([msg] (request msg nil))
  ([msg askin]
     (io!
      (if (pid? (:pid @*s*)) 
	(! (:pid @*s*) ["REQUEST" msg askin])
	(deliver askin (list (new-mql-error +error-clj-service-dead+)))))))  
 
;;FIXME: invalid messages, like array of ints? 
;;we added a debug message
;;so when we get the really annoying failures to
;;stop we can examine this:)

;;TODO: map of options 
(defn receive
  ([msg] (receive msg 3 false))
  ([msg resend] (receive msg resend false))
  ([msg resend wait-on]
     (when-not (invalid-msg? msg)
       (let [ask-obj (promise)]
	 (request msg ask-obj)
	 (loop [i 0 ask ask-obj]
	   (if (wait-for ask 5000) 
	     (let [result @ask] 
	       (cond 
		(instance? Exception result) (throw result)
		(or result (false? result)) result
		true (throwf "invalid result received %s" result))) 
	     (if (and resend (< i resend)) 
	       (let [ask (promise)]
		 (debug "resending message %s" msg)
		 (recur (+ i 1) ask))
	       (if wait-on
		 (do (debug "too much time for msg %s" msg)
		     (recur (+ i 1) ask))
		 (do (debug "too much time for msg %s... ending request" msg)
		     (list (new-mql-error +error-socket-repeat+)))))))))))  

