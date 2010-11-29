;;take from 
;;https://github.com/jochu/swank-clojure/tree/master/src/swank/util/concurrent/

(ns utils.mbox
  ;(:refer-clojure :exclude [send get])
  (:use utils.general))

;; Holds references to the mailboxes (message queues)
(defn- _get
  "Returns the mailbox for a given id. Creates one if one does not
   already exist."
  ([p id]
     (dosync 	     
      (when-not (get @(:boxes p) id)
        (alter (:boxes p) assoc
               id (java.util.concurrent.LinkedBlockingQueue.))))
     (@(:boxes p) id))
  {:tag java.util.concurrent.LinkedBlockingQueue})

(defn- _send
  "Sends a message to a given id."
  ([p id message]
     (let [mbox (_get p id)]
       (.put mbox message))))

;;mailbox is BAD - .take can occur if it is empty, and it seems somehow to stall!
(defn- _receive
  "Blocking recieve for messages for the given id."
  ([p id] 
     (let [mb (_get p id)] 
       (.take mb))))

(defprotocol #^{:private true} MAIL (ge [p id]) (pu [p id msg]))
(defrecord+ mbox [[ boxes (ref {})]]
  new-mbox
  MAIL
  (pu [p id msg] (_send p id msg))
  (ge [p id] (_receive p id)))

