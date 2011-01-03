;; forex.util.general - general utilities for clj-forex

(ns forex.util.general
  (:use utils.general forex.util.spawn forex.util.log)
  (:import (org.joda.time DateTime DateTimeZone Instant)))

(defn symbolicate
  "symbolicate symbols together. ignores things like whitespaces, just drops them!"
  [& args]
  (symbol (apply str args)))


;;TODO: add support for waiting on multiple objects, including sockets!
(defprotocol PWait
  (wait-for [this timeout units] [this timeout]))
;;copied from clojure source, but adding timeout wait-for
(defn beg
  "Alpha - subject to change.
  Returns a promise object that can be read with deref/@, and set,
  once only, with deliver. Calls to deref/@ prior to delivery will
  block. All subsequent derefs will return the same delivered value
  without blocking."
  {:added "1.1"}
  []
  (let [d (java.util.concurrent.CountDownLatch. 1)
        v (atom nil)]
    (reify 
      clojure.lang.IDeref
      (deref [_] (.await d) @v)
      PWait
      (wait-for [this timeout]
		(wait-for this timeout
			  java.util.concurrent.TimeUnit/MILLISECONDS))
      (wait-for [this timeout units]
		(if timeout
		  (.await d timeout units)
		  (do (.await d) true)))
      clojure.lang.IFn
      (invoke [this x] 
	      (locking d
		(if (pos? (.getCount d))
		  (do (reset! v x)
		      (.countDown d)
		      x)
		  (throw
		   (IllegalStateException.
		    "Multiple deliver calls to a promise"))))))))

(defn give
  "Alpha - subject to change.
  Delivers the supplied value to the promise, releasing any pending
  derefs. A subsequent call to deliver on a promise will throw an exception."
  {:added "1.1"}
  [promise val]
  (promise val))



(defmacro awhen [test & body]
  `(when-let [~'it ~test]
     ~@body))
 
(defmacro naive-var-local-cache-strategy [var] 
 `(let [cache# (atom {})]
    (reify PCachingStrategy
      (retrieve [_ item#] (get @cache# item#))
      (cached? [_ item#] (contains? @cache# item#))
      (hit [this# _] this#)
      (miss [this# item# result#]
	    (reset! cache# (swap! ~var assoc item# result#))
	    this#))))

(defmacro constants [& args]
  `(do ~@(map (fn [[name val]] `(def ~name ~val)) (group args 2))))

(defn now [] (DateTime. DateTimeZone/UTC))
 
(defn abs
  ([] (int (/ (.getMillis (Instant. (now))) 1000)))
  ([date] (int (/ (.getMillis (Instant. date)) 1000))))

(defmacro spawn-log [func]
  `(spawn (fn [] (try (~func) (catch Exception e#
				(.printStackTrace e#) (severe e#))))))

(defonce *env* (atom {:timeframe 1440 :index 0})) ;default +D1+
(defn env [key] (key @*env*))
(defn env! [map]
  (swap! *env* #(merge % map))
  map)

;;todo: fix private!
;;todo: ignores all nils?
(defmacro wenv [[& args] & body]
  `(binding [forex.util.general/*env*
	     (atom (merge @@~#'*env* (hash-map ~@args)))]
     ~@body))

(defmacro with-write-lock [l & body]
  `(let [obj# ~l]
     (try (do (.lock (.writeLock obj#)) ~@body)
	  (finally (.unlock (.writeLock obj#))))))

(defmacro with-read-lock [l & body]
  `(let [obj# ~l]
     (try (do (.lock (.readLock obj#)) ~@body)
	  (finally (.unlock (.readLock obj#))))))

