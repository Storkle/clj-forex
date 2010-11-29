;todo: better printing of records!
(ns forex.zmq
  (:require [org.zeromq.clojure :as z]
	    [utils.mbox :as m])
  (:use utils.general forex.utils))

; Constants
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

(defonce- *ctx* (z/make-context 1))
(def *stop* (gensym))
(defn make-poller [context socket]
  (let [p (z/make-poller context 1)]
    (z/register p socket)
    p))

(defprotocol SOCKET
  (connect [p args])
  (alive? [p])
  (finalize [p])
  (bind [p]))
(defprotocol SERVER
  (pu [p msg]))
(defprotocol CLIENT
  (ge [p id]))

(defrecord+ zmq-server
  [[_thread (atom nil)] [ _bound (atom nil)] [_killed (atom nil)]
   [_mail (atom (java.util.concurrent.LinkedBlockingQueue.))]
   socket [host "127.0.0.1"] port type poll]
  _new-server)

(defrecord+ zmq-client
  [[_thread (atom nil)] [ _bound (atom nil)] [_killed (atom nil)] 
   [_mbox (m/new-mbox)] 
   socket [host "127.0.0.1"] port type hwm [timeout 100] poll]
  _new-client)


(defn new-client [info type]
  (let [{port :port host :host timeout :timeout} info]
    (is? (and port type) "invalid params (port temp) (%s %s)" port
	 type)
    (let [socket (z/make-socket *ctx* type)]
      (_new-client :host (or host "127.0.0.1")
		   :port port :socket socket
		   :type type :poll (make-poller *ctx* socket)
		   :timeout (or timeout 100)))))

(defn socket-receive
  ([sock poll] (socket-receive sock poll 0))
  ([sock poll timeout] 
     (if (= timeout 0)
       (if-let [dat (z/recv sock z/+noblock+)]
	 (String. dat)) 
       (do  
	 (.setTimeout poll (* 1000 timeout))
	 (when (not (zero? (z/poll poll))) 
	       (String. (z/recv sock z/+noblock+)))))))

;;todo: what would be much better is an abstraction like in erlang in lisp
;;in which we actually have a 'message' - later?
(defn client-connect [p func]
  (if (alive? p)
    (throwf "client is already alive and well!")
    (do
      (let [{:keys [socket _thread _mbox _killed poll timeout ]} p] 
	(bind p)
	(! _thread 
	   (thread
	     (try
	       (loop []
		 (when (not @_killed)
		   (if-let [msg (socket-receive socket poll timeout)] ;(println msg)
		     (apply m/pu _mbox (func msg)))
		   (recur))) 
	       (finally (.close socket) (! _killed true)))))))))
		
(extend zmq-client
  SOCKET
  {:connect client-connect
   ;;TODO: how do we make all of this thread safe across multiple binds, finalizes, etc???
   ;;it has side effects, so i guess we have to use locks? hmm... is that the only way?   
   :finalize #(cond
		@(:_killed %) nil
		(not (alive? %)) (do (.close (:socket %)) true) 
		true (do (! (:_killed %) true)
			 (.join @(:_thread %))
			 true))
   :alive? #(if (not @(:_thread %))
	      false 
	      (.isAlive @(:_thread %)))
   :bind #(if @(:_killed %)
	    (throwf "socket has been killed: cant reuse")
	    (when (not @(:_bound %))
	      (z/connect (:socket %) (str "tcp://" (:host %) ":" (:port %)))
	      (! (:_bound %) true)))}
  CLIENT
  {:ge (fn [p id] (m/ge (:_mbox p) id))})

(defn new-server [info type]
  (let [{port :port host :host} info]
    (is? (and port type) "invalid params (port temp) (%s %s)" port
	 type)
    (let [socket (z/make-socket *ctx* type)]
      (_new-server :host (or host "127.0.0.1")
		   :port port :socket socket
		   :type type :poll (make-poller *ctx* socket)))))



;;todo: what if more than two threads call this? well, this wont happen, but still!
(defn server-connect [p args]
  (if (alive? p)
    (throwf "server is already alive and well!")
    (do
      (let [{:keys [port host socket _thread _mail _killed]} p] 
	(bind p)
	(! _thread
	   (thread
	     (try
	       (loop []
		 (let [msg (.take @_mail)]
		   (if (not (= msg *stop*))
		     (do
		       (z/send- socket (.getBytes msg))
		       (recur)))))
	       (finally (.close socket) (! _killed true)))))))))

;;todo: how can we reuse this server - you know? thread safely?
(extend zmq-server
  SERVER {:pu #(if (alive? %)
		 (.put @(:_mail %) %2)
		 (throwf "server isnt alive"))}
  SOCKET
  {:connect server-connect
   ;;TODO: how do we make all of this thread safe across multiple binds, finalizes, etc???
   ;;it has side effects, so i guess we have to use locks? hmm... is that the only way? 
   :finalize #(cond
		@(:_killed %) nil
		(not (alive? %)) (do (.close (:socket %)) true) 
		true (do (pu % *stop*)
			 (.join @(:_thread %))
			 true))
   :alive? #(if (not @(:_thread %))
	      false 
	      (.isAlive @(:_thread %)))
   :bind #(if @(:_killed %)
	    (throwf "socket has been killed: cant reuse")
	    (when (not @(:_bound %))
	      (z/bind (:socket %) (str "tcp://" (:host %) ":" (:port %)))
	      (! (:_bound %) true)))})


 

 
 