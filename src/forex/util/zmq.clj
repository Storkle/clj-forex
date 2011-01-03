(ns forex.util.zmq
  (:import (org.zeromq ZMQ))
  (:use utils.general))

					; Constants
(def +noblock+ 1)

(def +p2p+ 0)
(def +pub+ 1)
(def +sub+ 2)
(def +req+ 3)
(def +rep+ 4)
(def +xreq+ 5)
(def +xrep+ 6)
(def +pull+ 7)
(def +push+ 8)

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
(def +more+ 2)

;;Context
(defn new-context [io-threads]
  (ZMQ/context io-threads))
(defonce *context* (new-context 1))

(defprotocol PSocket
  (raw [this])
  (recv [this flags] [this])
  (bind [this address])
  (connect [this address])
  (hasReceiveMore [this])
  (close [this])
  (snd [this msg flags]))
(defprotocol PPoller
  (setTimeout [this timeout])
  (poll [this])
  (register [this socket])
  (getSocket [this i])
  (getSize [this])
  (pollin [this i])) 
(defrecord Poller [poll sockets]
  PPoller 
  (getSize [this] (.getSize (:poll this)))
  (pollin [this i] (.pollin (:poll this) i))
  (setTimeout [this timeout] (.setTimeout (:poll this) timeout))
  (poll [this] (.poll (:poll this)))
  (register [this socket]
	    (.register (:poll this)
		       (if (extends? PSocket (class socket))
			 (.raw socket)
			 socket))
	    (swap! (:sockets this) conj socket))
  (getSocket [this i] (nth @(:sockets this) i)))

(defn new-poller
  ([sockets] (new-poller *context* sockets))
  ([context sockets]
     (let [p (Poller. (.poller context (count sockets)) (atom []))]
       (.setTimeout p -1)
       (on [sock sockets]
	   (.register p sock))
       p)))

(defrecord Socket [socket]
  PSocket
  (raw [this] (:socket this))
  (snd [this msg flags] (.send socket (.getBytes msg) flags))
  (recv [this flags] (String. (.recv socket flags)))
  (recv [this] (recv this 0)) 
  (close [this] (.close socket))
  (bind [this address] (.bind socket address))
  (connect [this address] (.connect socket address))
  (hasReceiveMore [this] (.hasReceiveMore socket)))
 
(defn new-socket
  ([type] (new-socket *context* type))
  ([context type] (Socket. (.socket context type))))

(comment
  (defn new-poll
    ([sockets] (new-poll *context* sockets))
    ([context sockets]
       (let [p (.poller context (count sockets))]
	 (.setTimeout p -1)
	 (on [sock sockets]
	     (.register p (.socket sock)))
	 p)))

  (defn new-socket
    ([socket-type]
       (new-socket *context* socket-type))
    ([context socket-type ]
       (.socket context socket-type))))
