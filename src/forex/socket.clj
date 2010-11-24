;;Assertion failed: pending_term_acks (socket_base.cpp:690)

(ns forex.socket
   (:refer-clojure :exclude (=))
  (:require [org.zeromq.clojure :as z])
  (:use forex.utils))
;;todo: how to handle closing of server?
;;todo: store all historic price data in a database in order to quicken access!
(defonce- *ctx* (z/make-context 1))
;;using socket:todo make bi and thread safe
(defn connect-socket [data]
  (let [s (z/make-socket *ctx* z/+req+)]
    (z/connect s (str "tcp://" (:host data) ":" (:port data)))
    {:socket s :host (:host data) :port (:port data)}))
(defn write-socket [conn msg]
  (is? conn "write-socket: no connection provided")
  (z/send- (:socket conn) (.getBytes msg)))
(defn receive-socket [conn]
  (is? conn "receive-socket: no connection provided")
  (split (String. (z/recv (:socket conn))) #" +"))
;;clj-forex receive
(defn receive [msg]
  (write-socket (env :socket) msg)
  (let [result (receive-socket (env :socket))] result))

;;hash all connections
(defonce- *connections* (atom {}))
(defn get-connection [port]
  (get @*connections* port))

(defn connect
  ([] (connect 2027))
  ([port-id]
     (let [port (get  @*connections* port-id)]
       (if port
	 (throwf "port already exists and is active - cannot connect!")
	 (let [socket (connect-socket {:host "localhost" :port port-id})]
	   (env! {:socket socket})
	   (swap! *connections* assoc port-id socket))))))

(defn disconnect
  ([] (disconnect 2027))
  ([port-id]
     (let [socket (get @*connections* port-id)]
       (when socket 
	 (.close (:socket socket))
	 (swap! *connections* dissoc port-id)
	 (env! {:socket nil})
	 true))))




