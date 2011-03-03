
;;inspired by gambit-c termite's syntax
(ns forex.util.fiber.spawn
  (:use forex.util.general)
  (:require [forex.util.fiber.mbox :as m]))

;;TODO: will eventually integrate with zeromq and will be able to be 'reactive' so we avoid
;;java's thread pool limit (or somehow get kilim to work?)
;;TODO: also, should we allow functions like ?? to discard messages? Perhaps not?
;;TODO: user settable default return value?
;;TODO: linking, remote nodes
;;utils
(defonce- *mailboxes* (atom {}))
(defn- make-mailbox [tag]
  (let [mb (m/new-mbox)]
    (swap! *mailboxes* assoc tag mb)
    mb))
(defonce- *threads* (atom {}))

(defn- remove-pid [pid]
  (when pid (swap! *threads* dissoc pid) (swap! *mailboxes* dissoc pid)))
(defn- get-mailbox [tag]
  (get @*mailboxes* tag))
(defn pid? [pid]
  "test for valid pid"
  (if (get-mailbox pid) true false))

(defonce- *mb* (ThreadLocal.))
(defonce- *self* (ThreadLocal.)) 
(defn- mb [] (.get *mb*)) 

;; functions
(let [r (java.util.Random.)]
  (defn make-tag []
    "produce a random tag/pid"
    (Long/toString (Math/abs (.nextLong r)) 36)))

(defn ?? []
  (m/?? (mb) nil))
(defn- throwf [& args] (throw (Exception. (apply format args))) )

(defn !
  "send a message to a pid, with an option id which the message will be placed in"
  ([pid msg] (! pid nil msg))
  ([pid id msg]
     (if-let [mbox (get-mailbox pid)]
       (m/! mbox id msg)
       (throwf "unknown pid %s" pid))))
(binding [*out* *out*] (defn- log [& args] (println (apply format args))))
(defn self []
  "returns pid of current spawn or nil if it isnt a spawnage"
  (.get *self*))
(defn spawn
  ([thunk] (spawn thunk nil))
  ([thunk name]
     (let [tag (make-tag)
           mail (make-mailbox tag)]
       (let [thread (let [thread (Thread.
                                  (bound-fn
                                   [] 
                                   (try 
                                     (do (.set *mb* mail) (.set *self* tag)
                                         (thunk))
                                     (catch Exception e
                                       (log "pid %s error: %s " (self) e)
                                       (.printStackTrace e)
                                       )
                                     (finally (remove-pid tag)))))]
                      (if name (doto thread (.setName name)) thread))]
         (swap! *threads* assoc tag thread)
         (.start thread))
       tag)))

(defn- assert-spawn [] (when (not (self)) (throwf "no local spawn available")))
;;does work - but we dont really need it!

(defn spawn-in-repl []
  "setup current thread as a spawned thread with mailbox"
  (let [tag (make-tag)]
    (remove-pid self)
    (.set *mb* (make-mailbox tag)) (.set *self* tag) (swap! *threads* assoc tag (Thread/currentThread))
    tag)) 

(def- *?* nil)

(defn ?
  "receive"
  ([] (? nil nil))
  ([timeout] (? timeout nil))
  ([timeout default] 
     (assert-spawn)
     (or (m/? (mb) nil timeout) default))) 

(defmacro recv- [item & clauses]
  "match item with caluses"
  (let [msg-gen (gensym)]
    `(let [~msg-gen ~item]
       (cond-match ~@(mapcat (fn [[test body]]
                               `[[~test ~msg-gen] ~body]) (group clauses 2))))))
(defmacro recv [& clauses]
  "receive a message from current spawned thread mailbox"
  `(let [msg# (?)]
     (recv- msg# ~@clauses)))
(defmacro recv-if [& clauses]
  "receive only if a message is in queue"
  `(let [msg# (? 0)]
     (recv- msg# ~@clauses)))

(comment
  ;;until we figure out why soft-timeout doesnt work... we're leaving this here!
  (defn ??
    "asynchronous with timeout: wait for a receive filtered by the function, discarding any other messages received"
    ([function] (?? function nil))
    ([function timeout]
       (soft-timeout! *?* timeout
         (loop []
           (let [msg (?)]
             (if-let [it (function msg)]
               msg
               (recur))))))))

(comment
  ;;until we figure out why soft-timeout doesnt work... we're leaving this here!
  (defn !?
    "asynchronous with timeout: send message with a tag and then receive back, discarding any other messages received"
    ([pid data] (!? pid data nil))
    ([pid data timeout]
       (soft-timeout! *?* timeout
         (let [tag (make-tag)]
           (! pid [(self) tag data])
           (loop []
             (let [msg (?)]
               (recv- msg
                 [(= ? tag) ?response] response
                 ? (recur)))))))))


(defn stop-all 
  "stops all threads spawned. probably only useful for debugging, and assuming they respond to stop"
  []
  (map #(! % "stop") (keys @*threads*)))

(defn kill-all
  []
  (map #(.stop %) (vals @*threads*)))


