(ns forex.indicator (:use forex.backend.core utils.general forex.utils))

(defn open
  ([] (open 0)) 
  ([i]
     (.open (get-stream (env :symbol) (env :timeframe)) i)))

(defn high
  ([] (high 0))
  ([i]
     (.high (get-stream (env :symbol) (env :timeframe)) i)))

(defn low
  ([] (low 0)) 
  ([i]
     (.low (get-stream (env :symbol) (env :timeframe)) i)))

(defn close
  ([] (close 0))
  ([i]
     (.close (get-stream (env :symbol) (env :timeframe)) i)))
