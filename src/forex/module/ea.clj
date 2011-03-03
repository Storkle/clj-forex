
;;TODO: it would be awesome if we could directly modify vars of an ea. but we cant, we can only stop , merge, start. TOOD: lookup how set! works! 

(clojure.core/use 'nstools.ns)
(ns+ forex.module.ea 
     (:clone clj.core) 
     (:use forex.util.core clj.io
           forex.util.general 
           forex.util.spawn forex.util.emacs
           clojure.contrib.core)
     (:import clojure.lang.Atom)
     (:require [clj-time.core :as t])
     (:use  
      forex.util.emacs
      forex.util.log  
      forex.module.error 
      [forex.module.indicator :exclude [alive? start stop]] 
      forex.module.account)
     (:import forex.util.core.AtomHash
	      java.util.concurrent.TimeUnit))
  
(defvar save-file nil) 
(defvar ea-on-exit-hook)
(defvar ea-on-start-hook)
(defonce *ea-registry* (atom []))
(defn register-ea []
  (swap! *ea-registry*
         (fn [old]
           (filter #(find-ns (symbol %))
                   (if (some #(= % (str *ns*)) old)
                     old
                     (conj old (str *ns*)))))))

;;saving objects to strings and going back -slow, but it works
(import (java.io ByteArrayOutputStream ObjectOutputStream
		 ByteArrayInputStream ObjectInputStream)
	org.apache.commons.codec.binary.Base64)
(defn bytes-to-obj
  "convert bytes to object"
  [obj]
  (.readObject
   (ObjectInputStream.
    (ByteArrayInputStream. obj))))
(defn obj-to-bytes
  "convert object to bytes"
  [obj]
  (with-open [bos (ByteArrayOutputStream.)
	      stream (ObjectOutputStream. bos)]
    (.writeObject stream obj)
    (.flush stream) 
    (.toByteArray bos))) 
(defn encode [o]
  (Base64/encodeBase64String (obj-to-bytes o)))
(defn decode [o]
  (bytes-to-obj (Base64/decodeBase64 o)))
(defmethod print-dup :default [o w]
  (.write w (str "#=(forex.module.ea/decode \"" (encode o) "\")")))  
;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; 


(defn- get-fn [a] (if (var? a) (var-get a) a)) 
(defonce *ea* nil)
(defonce *args* nil) 
(defmacro with-ea [[ea & name] & body]
  `(let [ea# ~ea]
     (debugging (format "%s %s:" ~(or (first name) "") (:name ea#))
                (binding [*ea* ea# *args* (:args ea#)]
                  (wenv {:symbol (:symbol ea#) :period (:period ea#)}
                        ~@body)))))
(defmacro locking-read[lock & body]
  `(let [l# (.readLock ~lock)]
     (try (do (.lock l#) ~@body)
          (finally (.unlock l#)))))
;;TODO: for some reason, write lock never can lock
(defmacro locking-write [lock & body]
  `(let [l# (.writeLock ~lock)]
     (try (do (.lock l#) ~@body)
          (finally (.unlock l#)))))
(defmacro locking-write-timeout [[ lock timeout] & body]
  `(let [l# (.writeLock ~lock)]
     (if (.tryLock l# ~timeout TimeUnit/MILLISECONDS)
       (try (do  ~@body)
            (finally (.unlock l#))))))


(deferror *ea-stop* [*clj-forex-error*] [message]
  {:msg (str "ea stop: " message)})


(defn exit
  ([msg] (raise *ea-stop* msg))
  ([msg & args] (raise *ea-stop* (if (string? msg)
                                   (apply format msg args)
                                   (concat [msg] args)))))



;;TODO: do we really want to copy these objects? yes, in most cases perhaps
;;but we should add an option to the ea definition (we will do that later, before first release)
(defmulti copy-ea-obj class)
(defmethod copy-ea-obj :default [o] o)
(defmethod copy-ea-obj clojure.lang.Atom [o] (atom (copy-ea-obj @o)))
(defmethod copy-ea-obj clojure.lang.Ref [o] (ref (copy-ea-obj @o)))
(defmethod copy-ea-obj forex.util.core.AtomHash [o] (atom-hash (copy-ea-obj @o)))

(defn- copy-ea-objs [map]
  (apply hash-map
         (mapcat (fn [[key val]]
                   (list key (copy-ea-obj val)))
                 map)))

;;##ea implementation
(defonce *eas* (atom []))
(import java.util.concurrent.locks.ReentrantReadWriteLock)
;;TODO: lock orphaning
(defonce *ea-pre-lock*
  (java.util.concurrent.locks.ReentrantReadWriteLock. true))

(defn every [pred coll]
  (if (empty? coll)
    false
    (loop [a coll]
      (if (empty? a)
        true
        (if (not (pred (first a)))
          false
          (recur (rest a)))))))

(defn query [m]
  (let [a (filter
           (fn [ea]
             (if (every (fn [[key val]]
                          (= val (get ea key)))
                        m) 
               ea))
           @*eas*)]
    (if (= (count a) 1) (first a) a)))
(defn alive? [ea] (pid? (:pid ea)))

(defmacro- catch-unexpected [prefix & body]
  `(try (do ~@body)
        (catch Exception e#
          (severe "%s - caught unexpected error %s" ~prefix e#))))


(require 'clojure.contrib.error-kit)
;;TODO; how to create unbound var for error-kit???
(defn ping [a] (! (:pid a) "PING"))
(defn ping-all [] (doall (map ping @*eas*)))
;;TODO: we need a monitor which pings and then sets something.... like in erlang

(defn run-by-tick [{:keys [deinit init start] :as ea}]
  (with-ea [ea]   
    (try
      (run-hooks ea-on-start-hook)
      (clojure.contrib.error-kit/with-handler
        (loop [prev-close nil]
          (sleep 1) 
          (when-not
              (match (? 0) 
                     "STOP" true
                     "PING" (do (out "ping") nil)) 
            (let [new-close (close)]  
              (when-not (= new-close prev-close)
                (let [func (get-fn start)]
                  (if (fn? func)
                    (locking-read *ea-pre-lock*
                                  (func (:args ea)))
                    (warn "%s is not a function. start cannot be called" func))))
              (recur new-close))))
        (clojure.contrib.error-kit/handle *ea-stop* [message] (out "stopping ea ... %s" message)))  
      (catch Exception e 
        (severe "stopping ea... caught exception %s" e)
        (reset! (:exit ea) e)
        (.printStackTrace e))
      (finally
       (when-not @(:exit ea) (reset! (:exit ea) true))
       (info "running deinit ...")
       (catch-unexpected
        "deinit" 
        (let [de (get-fn deinit)]
          (if (fn? de)
            (de)
            (warn "deinit %s is not a function. ignoring ...." de))))))
    (run-hooks ea-on-exit-hook)))

(defn- timeframe? [a] (number? a))
(defrecord EA [name type ns init deinit start symbol period args
               pid run vars exit]) 

(defn- constant-map [& args] {})
(defn- constant-true [& args] {})
(defn new-ea
  ([] (new-ea {}))
  ([{:keys [ns symbol period run args vars] :or {symbol (env :symbol)
                                                 run run-by-tick
                                                 vars {}
                                                 period (env :period)}}]
     (let [ns (cond
               (nil? ns) *ns*
               (symbol? ns) (find-ns ns)
               (string? ns) (find-ns (symbol ns))
               true ns)]
       (is? (ns? ns))
       (is? [(map? vars) (every? var? (keys vars))])
       (let [name (str (ns-name ns))
             start (ns-symbol 'start ns)
             init (or (let [fn (ns-symbol 'init ns)]
                        (if (get-fn fn) fn))
                      constant-map)
             deinit (or (let [fn (ns-symbol 'deinit ns)]
                          (if (get-fn fn) fn))
                        constant-true)] 
         (is? [(fn? (get-fn init)) (fn? (get-fn deinit)) (string? name)
               (string? symbol) (fn? run) (timeframe? period)])
         (EA. (format "%s %s, %s" name symbol period) (last (.split name "\\."))
              ns
              init deinit start symbol period (or args {})
              nil run
              (merge (copy-ea-objs (ns-vars ns)) vars)
              (atom 0)))))) 

;;how to get it to access actual var? as long as we dont use set!
(defmethod clojure.core/print-method EA [o w]
  (.write w  (format "<EA \"%s\" %s %s |%s|>"
                     (:name o) (pid? (:pid o))
                     (:args o) (:vars o))))

;;TODO: pid without spawn!!

(defn run-start [ea]
  (let [ea (merge ea {:exit (atom false)})]
    (with-ea [ea "START"] 
      (with-bindings (:vars ea)
	(let [new-ea (merge ea {:pid (spawn
				      #((:run ea) ea)
				      (:name ea))})]
	  (swap! *eas* conj new-ea) 
	  new-ea)))))


(defn restart [ea]  
  (when (not (alive? ea))
    ;;TODO: for some reason, if we change it to (hash old), it screws up! what!
    (let [old (swap! *eas*
                     (fn [old] (doall (filter #(not (= (hash ea) (hash %))) old))))]
      (try
        (run-start ea)
        (catch Exception e (swap! *eas* conj ea)
               (.printStackTrace e)
               (out "caught exception when running start %s" e))))))

;;TODO: check return type
(defn run-init [ea]
  (with-ea [ea "INIT"]
    (let [result ((get-fn (:init ea)) (:args ea))]
      (if (map? result)
        (merge ea {:vars (merge (:vars ea) result)})))))

(defn run-all [ea]
  (try
    (with-ea [ea "ALL"]
      (let [new-ea (run-init ea)]
        (if (instance? EA new-ea)
          (run-start new-ea)))
      (catch Exception e
        (out "caught exception %s" e)))))

(defn- filter-map [f map]
  (let [vals (mapcat identity (filter (fn [[key val]]
                                        (f key)) map))]
    (if (empty? vals) {} (apply hash-map vals))))

(defn run
  ([] (run *ns* {}))
  ([args] (run *ns* args))
  ([ns args] 
     ;;todo: not default, no nil
     (run-all
      (new-ea {:ns ns 
	       :args (filter-map
		      #(and (not (var? %))
			    (not (#{:symbol :period} %))) args)
	       :vars (filter-map var? args)
	       :symbol (or (:symbol args) (env :symbol))
	       :period (or (:period args) (env :period))}))))

(defn sym [] (:symbol *ea*))
(defn period [] (:period *ea*))
;;TODO: wait till it stops and delete
(defn stop [ea]
  (let [stop-it (fn [e]
                  (if (pid? (:pid e))
                    (do (! (:pid e) "STOP") 
                        true)))]
    (if (map? ea)
      (stop-it ea)
      (map stop-it ea))))

(defn clear-eas [] (count (reset! *eas* (filter alive? @*eas*))))
;;SAVE/LOAD 
(defn save-eas
  ([] (save-eas save-file 1000))
  ([save-file] (save-eas save-file 1000))
  ([save-file timeout]
     (debugging "Save EA:"
		(try
		  (locking-write-timeout [*ea-pre-lock* (or timeout 1000)] 
					 (info "saving ...")
					 (frm-save save-file @*eas*)
					 (count @*eas*))
		  (catch Exception e
		    (warn "failed to save eas %s" e))))))

(defn load-eas 
  ([] (load-eas save-file false))
  ([append] (load-eas save-file append))
  ([file append]
     (debugging "Load EA:"
		(try
		  (let [eas (frm-load file)]
		    (is? [(or (empty? eas) (every #(instance? EA %) eas))])
		    (if append (do (swap! *eas* concat eas) (count eas)) (vec eas)))
		  (catch Exception e
		    (warn "failed to load eas %s %s: %s" file append e))))))


