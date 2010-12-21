;;forex.util.zmq - wrapper around clojure wrapper for zmq - used in mql backend for socket communication with metatrader

;;TODO: replace zeromq clojure library we use and replace with our own
(ns forex.util.zmq
  (:require [org.zeromq.clojure :as z]
	    [utils.fiber.mbox :as m])
  (:use  forex.util.general utils.general))

;; Constants 
(def +noblock+ 1)

(def +p2p+ 0)
(def +pub+ 1)
(def +sub+ 2)
(def +req+ 3)
(def +rep+ 4)
(def +xreq+ 5)
(def +xrep+ 6)
(def +upstream+ 7)
(def +downstream+ 8)

(def +hwm+ 1)
(def +lwm+ 2)
(def +swap+ 3)
(def +affinity+ 4)
(def +identity+ 5)
(def +subscribe+ 6)
(def +unsubscribe+ 7)
(def +rate+ 8)
(def +recovery-ivl+ 9)
(def +mcast-loop+ 10)
(def +sndbuf+ 11)
(def +rcvbuf+ 12)

(def +pollin+ 1)
(def +pollout+ 2)
(def +pollerr+ 4)
;; 
(defonce- *ctx* (z/make-context 1))
 
(defn socket-new-poller [socket]
  (let [p (z/make-poller *ctx* 1)]
    (z/register p socket) p))

(defn socket-new [info type]
  (let [{port :port host :host timeout :timeout} info]
    (is (and port type) "invalid params (port temp) (%s %s)" port
	type)
    (let [socket (z/make-socket *ctx* type)]
      ;(z/connect socket (str "tcp://" host ":" port))
      socket)))

(defn socket-receive
  ([sock poll] (socket-receive sock poll 0))
  ([sock poll timeout] 
     (if (= timeout 0)
       (if-let [dat (z/recv sock z/+noblock+)]
	 (String. dat)) 
       (do  
	 (.setTimeout poll (* 1000 (or timeout 100)))
	 (when (not (zero? (z/poll poll))) 
	       (String. (z/recv sock z/+noblock+)))))))


 
 
