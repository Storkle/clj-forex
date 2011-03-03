
;;modified from
;;https://github.com/jochu/swank-clojure/tree/master/src/swank/util/concurrent/
(ns forex.util.fiber.mbox
  (:use forex.util.general))

;; Holds references to the mailboxes (message queues)
(defn- _get
  "Returns the mailbox for a given id. Creates one if one does not
   already exist."
  ([p id]
     (dosync         
      (when-not (get @(:boxes p) id)  
        (alter (:boxes p) assoc
               id (java.util.concurrent.LinkedBlockingQueue.)))
      (@(:boxes p) id)))
  {:tag java.util.concurrent.LinkedBlockingQueue})

(defn !
  "Sends a message to a given id."
  ([p id message] 
     (let [mbox (_get p id)]
       (.put mbox message))))


(defn ??
  "poll in milliseconds"
  ([p] (?? p nil))
  ([p id]
     (let [mb (_get p id)]
       (.peek mb))))

(defn ?
  "poll in milliseconds"
  ([p id] (? p id nil))
  ([p id timeout]
     (is (or (not timeout) (and (number? timeout) (>= timeout 0))) "timeout must be nil or a positive number")
     (let [mb (_get p id)]
       (cond
         (not timeout) (.take mb)
         true (.poll mb timeout java.util.concurrent.TimeUnit/MILLISECONDS)))))

(defrecord+ mbox [[boxes (ref {})]]
  new-mbox)

