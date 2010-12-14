(ns forex.utils (:use utils.general utils.fiber.spawn forex.log)
    (:import (org.joda.time DateTime DateTimeZone Instant)))

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
  (swap! *env* #(merge % map)))

;;todo: fix private!
;;todo: ignores all nils?
(defmacro wenv [[& args] & body]
  `(binding [forex.utils/*env*
	     (atom (merge @@~#'*env* (hash-map ~@args)))]
     ~@body))



