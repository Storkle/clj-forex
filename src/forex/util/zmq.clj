
(clojure.core/use 'nstools.ns)
(ns+ forex.util.zmq
  (:clone clj.core)
  (:import (org.zeromq ZMQ))
  (:use forex.util.general))
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
  (hasMore [this])
  (close [this])
  (snd [this msg] [this msg flags])) 
(defprotocol PPoller
  (setTimeout [this timeout])
  (poll [this])
  (register [this socket])
  (getSocket [this i])
  (getSize [this])
  (pollin [this i])
  (pollout [this i])) 
(defrecord Poller [poll sockets] 
  PPoller 
  (getSize [_] (.getSize poll))
  (pollin [_ i] (.pollin poll i))
  (pollout [_ i] (.pollout poll i))
  (setTimeout [_ timeout] (.setTimeout poll timeout))
  (poll [_] (.poll poll))
  (register [_ socket]
	    (println "socket is " socket )
            (.register poll
                       ;;TODO: what if we reload? wont work?
                       (if (satisfies? PSocket socket)
                         (do (println "YAY " (raw socket)) (raw socket))
			 (do (println "DOESNT") socket)))
            (swap! sockets conj socket))
  (getSocket [_ i] (nth @sockets i)))

(defn new-poller
  ([sockets] (new-poller *context* sockets))
  ([context sockets]
     (let [p (Poller. (.poller context (count sockets)) (atom []))]
       (.setTimeout p -1)
       (on [sock sockets]
           (.register p sock))
       p)))
 
;;TODO: now just a string socket!
(defrecord StringSocket [^org.zeromq.ZMQ$Socket socket]
  PSocket 
  (raw [this] socket)
  (snd [this msg] (.snd this msg 0))
  (snd [this msg flags]
       (if (string? msg)
         (.send socket (.getBytes ^String msg) flags)
         (.send socket (byte-array msg) flags))) 
  (recv [this flags]
	(when-let [it (.recv socket flags)] (String. it)))
  (recv [this] (recv this 0)) 
  (close [this] (.close socket))
  (bind [this address] (.bind socket address))
  (connect [this address] (.connect socket address))
  (hasMore [this] (.hasReceiveMore socket))) 
(defrecord Socket [^org.zeromq.ZMQ$Socket socket]
   PSocket 
   (raw [this] socket)
   (snd [this msg] (.snd this msg 0))
   (snd [this msg flags] 
	(if (string? msg)
	  (.send socket (.getBytes ^String msg) flags)
	  (.send socket (byte-array msg) flags))) 
   (recv [this flags]  (.recv socket flags))
   (recv [this] (recv this 0)) 
   (close [this] (.close socket))
   (bind [this address] (.bind socket address))
   (connect [this address] (.connect socket address))
   (hasMore [this] (.hasReceiveMore socket)))

(defn new-socket
  ([type] (new-socket *context* type))
  ([context type] (Socket. (.socket context type))))
(defn new-string-socket
  ([type] (new-socket *context* type))
  ([context type] (StringSocket. (.socket context type))))

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


