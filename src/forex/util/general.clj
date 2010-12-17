;; forex.util.general - general utilities for clj-forex

(ns forex.util.general
  (:use utils.general utils.fiber.spawn forex.util.log)
  (:import (org.joda.time DateTime DateTimeZone Instant)))


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
  `(spawn (fn [] (try (~func) (catch Exception e# (severe e#))))))

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

