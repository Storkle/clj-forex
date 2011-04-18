(ns ^{:author "Seth"} forex.backend.mql.utils  
  (:use forex.util.general)
  (:import (java.io DataInputStream ByteArrayInputStream))
  (:use forex.util.log))

(defonce *msg-id* (atom 0))

(defn msg-id
  "Generate a new message id string."
  []
  (str (swap! *msg-id* inc)))
  
(defn parse-int
  "Convert string to integer or return string if exception is thrown."
  [string]
  (try (Integer/parseInt string) (catch Exception e string)))

(defmacro catch-unexpected
  "Catch and warn on any unexpected errors."
  [& body]
  `(try (do ~@body)
        (catch Exception e# (.printStackTrace e#) (warn e#))))
  
(defn into-doubles
  "Convert a byte array into a clojure double vector."
  [^bytes array]
  (let [stream (DataInputStream. (ByteArrayInputStream. array))
	length (/  (alength array) 8)]
    (loop [i 0 v (transient [])]
      (if (< i length)
	(recur (inc i) (conj! v (.readDouble stream)))
	(persistent! v)))))  




