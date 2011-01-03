;;this is a generalization of util.fiber.spawn and will eventually replace it. it allows us to poll on not only local mailbox but also on sockets
(ns forex.util.spawn
  (:use utils.general forex.util.zmq)
  (:require [utils.fiber.spawn :as s]))
 
(def- *pid* (atom []))

(defalias pid? s/pid?)
(defalias self s/self)
(def kill-all s/kill-all)
(defalias spawn-in-repl s/spawn-in-repl) ;;TODO: add socket for this one
 
(defalias ? s/?)

(defalias make-tag s/make-tag)
(defn ! [pid msg]
  (with-open [local (doto (new-socket +push+)
		      (.connect (format "inproc://%s" pid)))]
    (s/! pid msg)
    (.snd local "REQUEST" +noblock+)))
(defn  stop-all []
  (swap! *pid*
	 (fn [old]
	   (map #(if (pid? %)
		   (! % "STOP")) @*pid*))))

 
(defrecord LocalSocket [socket]
  PSocket
  (raw [this] (.raw socket)) 
  (recv [this flags] 
	(let [r (.recv socket flags)]
	  (s/?)))
  (recv [this] (recv this 0))
  (close [this] (.close socket))
  (hasReceiveMore [this] false))


(comment
  (defn te []
   (def pid (spawn #(do (pr "BEFORE") (pr local)
			(pformat "local %s%n" (first (event-seq [local])))
			(pr "AFTER"))))))


(defonce- *local* (ThreadLocal.))
(defn- self-get [key]
  (let [map (.get *local*)]
    (when map 
      (map key))))
(defn- self-assoc [key obj]
  (let [map (.get *local*)]
    (if map 
     (.set *local* (assoc map key obj)))))
(def local nil)
(defn spawn [thunk]
  (let [pid (s/spawn (fn [] 
		       (.set *local* {})
		       (with-open
			   [local-socket (LocalSocket. 
				   (doto
				       (new-socket +pull+)
				     (.bind  (str "inproc://" (self)))))]	 
			 (binding [local local-socket]
			   (thunk)))))]
    (swap! *pid* concat [pid])
    pid))

(defmulti event-seq class)
(defmethod event-seq clojure.lang.IPersistentVector [v]
  (event-seq (new-poller v)))
(defmethod event-seq forex.util.zmq.Poller [p]
  ((fn the-seq [p] 
     (lazy-seq
      (let [amount (.poll p)] 
	(concat (for [i (range 0 (.getSize p)) :when (.pollin p i)]
		  (let [sock (.getSocket p i)
			msg 
			(loop [msg [(.recv sock 0)]]			
			  (if (.hasReceiveMore sock)
			    (recur (cons (String. (.recv sock 0)) msg))
			    msg))]
		    [sock (if (= (count msg) 1) (first msg) (vec msg))]))
		(the-seq p)))))
   p)) 

;;? with multiple sources or change to poll
;;!? (timeout)
;;?? (filter)


