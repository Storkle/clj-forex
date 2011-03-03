(ns forex.backend.mql.utils  
  (:use forex.util.general)
  (:import (java.io DataInputStream ByteArrayInputStream))
  (:use forex.util.log))

(defonce *msg-id* (atom 0))
(defn msg-id []
  (str (swap! *msg-id* inc)))
 
(defn parse-int [a]
  (try (Integer/parseInt a) (catch Exception e a)))
(defmacro catch-unexpected [& body]
  `(try (do ~@body)
        (catch Exception e# (.printStackTrace e#) (warn e#))))
 
(defn into-doubles [^bytes array] 
  (let [stream (DataInputStream. (ByteArrayInputStream. array))
	length (/  (alength array) 8)]
    (loop [i 0 v (transient [])]
      (if (< i length)
	(recur (inc i) (conj! v (.readDouble stream)))
	(persistent! v))))) 




