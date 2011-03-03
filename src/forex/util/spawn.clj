
(clojure.core/use 'nstools.ns)
(ns+ forex.util.spawn
     (:clone clj.core)   
     (:use forex.util.general forex.util.zmq)
     (:import forex.util.zmq.Poller)
     (:require [forex.util.fiber.spawn :as s]))

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
        (when (.recv socket flags)      
          (s/?)))  
  (recv [this] (recv this 0))
  (close [this] (.close socket))
  (hasMore [this] false))


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
(defn spawn
  ([thunk] (spawn thunk nil))
  ([thunk name] 
     (let [pid (s/spawn
                (fn [] 
                  (.set *local* {})
                  (with-open
                      [local-socket (LocalSocket. 
                                     (doto
                                         (new-string-socket +pull+)
                                       (.bind  (str "inproc://" (self)))))]        
                    (binding [local local-socket]
                      (thunk))))
                name)]
       (swap! *pid* concat [pid])
       pid)))

(comment
  (defn te []
    (def pid (spawn #(let [seq (event-seq [local])]
                       (pr (format "local %s %s%n"
                                   (first seq)
                                   (second seq))))))))





(defn recv-multi
  ([sock] (recv-multi sock 0))
  ([sock flags] 
     (let [first-msg (.recv sock flags)]
       (when first-msg 
         (if (.hasMore sock)
           (loop [msg [(.recv sock) first-msg]]
             (if (.hasMore sock)
               (recur (cons (.recv sock) msg))
               msg))
           first-msg)))))

(defn recv-all
  ([sock] (recv-all sock 0))
  ([sock flags]
     (let [first-msg (recv-multi sock flags)]
       (when first-msg
         (loop [messages [[sock first-msg]]]
           (if-let [new-msg (recv-multi sock +noblock+)]
             (recur (conj messages [sock new-msg]))
             messages))))))

;;TODO: fair queue? prevent too much messages hogging sequence? who knows! ...

(import forex.util.zmq.Socket forex.util.zmq.Poller)
(defn- event-seq* [^Poller p]
  (lazy-seq
   (let [amount (.poll p)]
     (concat
      ;;TODO: memory overflow with getting all messsages? probably not ....
      (doall
       (mapcat #(when (or (.pollin p %) (.pollout p %))
		  (recv-all (.getSocket p %))) 
               (range 0 (.getSize p))))
      (event-seq* p))))) 


(defmulti event-seq class)
(defmethod event-seq clojure.lang.IPersistentVector [v]
  (event-seq (new-poller v)))
(defmethod event-seq Poller [p] (event-seq* p)) 

;;? with multiple sources or change to poll
;;!? (timeout)
;;?? (filter)

