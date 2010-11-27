;;Assertion failed: pending_term_acks (socket_base.cpp:690)
;;TODO: utilize defrecord2, make thread safe - definitely note  safe right now even with locks - must be used in one thread only. later though it appears to
;;work right now....

(ns forex.socket
  (:require [org.zeromq.clojure :as z]
	    [utils.mbox :as m])
  (:import java.util.concurrent.locks.ReentrantLock)

  (:use forex.utils))
 
;;todo: how to handle closing of server?

(defonce- *ctx* (atom nil))

(defn make-poller [context socket]
  (let [p (z/make-poller context 2)]
    (z/register p socket)
    p))
(defn socket-connect [data type]
  (let [s (z/make-socket @*ctx* type)]
    (z/connect s (str "tcp://" (:host data) ":" (:port data)))
    {:type type :socket s :lock (ReentrantLock.)
     :poll (make-poller @*ctx* s)
     :host (:host data) :port (:port data)}))

(defn socket-bind [data type]
  (let [s (z/make-socket @*ctx* type)]
    (z/bind s (str "tcp://" (:host data) ":" (:port data)))
    {:type type :socket s :poll (make-poller @*ctx* s)
     :lock (ReentrantLock.)
     :host (:host data) :port (:port data)}))

(defn socket-receive
  ([sock] (socket-receive sock 0))
  ([sock timeout]
     (let [s (:socket sock)]
       (is? (:socket sock)
	    "socket-receive: no connection provided in socket structure %s"
	    sock)
       (locking (:lock sock)
	 (if (= timeout 0)
	   (if-let [dat (z/recv s z/+noblock+)]
	     (String. dat))
	   (do 
	     (.setTimeout (:poll sock) (* 1000 timeout))
	     (if (not (zero? (z/poll (:poll sock))))
	       (String. (z/recv s z/+noblock+)))))))))

(defn socket-send [sock msg]
  (let [s (:lock sock)]
    (is? (:socket sock) "socket-send: no connection provided in socket structure %s" sock)
    (locking s (z/send- (:socket sock) (.getBytes msg)))))

(def *id* (atom  0))

(defn receive [msg]
  (let [i (if (> @*id* 100000000)
	    (swap! *id* 0)
	    (swap! *id* inc))] (m/get (str i))
    (socket-send (env :pub) (str i " " msg)) 
    (m/receive (str i))))
   
(defn start-sub []
  (if (and (env :sub-thread) (not (.isAlive (env :sub-thread))))
    (env! {:sub-thread nil}))
  (if (env :sub-thread)
    (throwf "subscribe thread already alive!")
    (let [sub (env :sub)]
      (is? sub "not connected")
      (env! {:sub-thread 
	     (thread
	       (loop [] 
		 (if-let [raw-dat (socket-receive sub 100)]
		   (let [dat (split raw-dat #" +")]	      
		     (m/send (first dat) (rest dat))))
		 (recur)))}))))
   
(defn connect []
  (if (or (env :pub) (env :sub))
    (throwf "already connected!") 
    (do
      (! *ctx* (z/make-context 1))
      (let [pub (socket-bind {:host "127.0.0.1" :port 2045} z/+pub+)
	    sub (try
		  (socket-connect {:host "127.0.0.1" :port 2055} z/+sub+)
		  (catch Exception e
		    (.close pub) 
		    (throw e)))]
	(.subscribe (:socket sub) (.getBytes ""))
	(env! {:pub pub :sub sub})
	(start-sub)
	{:pub pub :sub sub}))))
     
;TODO: just killing a thread like that while it is waiting on a socket - is that good? probably not....

(defn disconnect []
  (let [sub (env :sub) sub-thread (env :sub-thread)
	pub (env :pub)]
    (.term @*ctx*)
    (if sub-thread (.stop sub-thread))
    (.close (:socket sub)) (.close (:socket pub))
    (! *ctx* nil)
    (env! {:sub nil :pub nil :sub-thread nil})))


 
 





