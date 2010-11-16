(ns forex.socket 
  (:use forex.utils)
  (:import  
   java.net.Socket
   (java.io PrintWriter InputStreamReader BufferedReader)))

(defn connect-socket [server]
  (let [socket (Socket. (:name server) (:port server))
	in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
	out (PrintWriter. (.getOutputStream socket))
	conn (ref {:in in :out out :socket socket})] 
    conn))

(defn write-stream [conn msg]
  (is conn "no connection provided")
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush))
  conn)
(defn receive-stream [conn]
  (is conn)
  (let [result (.readLine (:in @conn))] 
    (rest (split result #" +"))))


(defn wr [msg]
  (write-stream (env :socket) msg))

(defn Receive [msg]
  (write-stream (env :socket) msg)
  (let [result (receive-stream (env :socket))] result))
(defn receive [msg]
  (let [result  (Receive msg)]
    (if (= (first result) "error")
      (throwf (str "error" (second result)))
      result)))
(defonce- *connections* (atom {}))
(defn connect
  ([] (connect 2007))
  ([port-id]
     (let [port (get  @*connections* port-id)]
       (if (and (:socket port) (.isClosed (:socket port))) (swap! *connections* dissoc port-id))
       (if port
	 (throw (Exception. "port already exists and is active - cannot connect!"))
	 (let [socket (connect-socket {:name "localhost" :port port-id})]
	   (env! {:socket socket})
	   (swap! *connections* assoc port-id socket))))))

(defn disconnect
  ([] (disconnect 2007))
  ([port-id]
     (let [port (get @*connections* port-id)]
       (when port 
	 (.close (:socket @port)) (.close (:in @port)) (.close (:out @port))
	 (swap! *connections* dissoc port-id)
	 true))))







