(clojure.core/use 'nstools.ns)
(ns+ forex.module.indicator.service
     (:clone clj.core)
     (:use forex.module.indicator.util
	   forex.module.indicator
	   forex.module.indicator.map)
     (:use forex.util.emacs
	   [clj-time.core :exclude [extend start]]
	   clj-time.coerce
	   forex.util.spawn
	   forex.util.core
	   forex.util.log
	   forex.util.general
	   forex.module.account.utils 
	   forex.module.error)   
     (:require clojure.contrib.core
	       [forex.backend.mql.socket-service :as backend])) 

(defn enumerate [i] (map (fn [a b] [a b]) (keys i) (vals i)))
(defn clean-rates [] 
  (swap! *indicators*
	 (fn [old]
	   (apply hash-map
		  (mapcat identity
			  (for [[key {:keys [pid] :as val}] (enumerate old)]
			    (when-not (empty? (filter #(or (pid? %) (nil? %)) pid))
			      [key val]))))))
  (count (keys @*indicators*)))  


;;TODO: use from/to
;;TODO: why doesnt now work vs itime!!!
(defn refresh-rates
  ([] (refresh-rates (update-time)))
  ([now]
     (let [i @*indicators*
	   results (binding [*debug* false]
		     (receive-lst
		      (vec (reverse
			    (for [indicator (vals i)]
			      (indicator-protocol
			       (merge indicator {:from now})))))
		      0))]
       (if-not (or (e? results)
		   (and (e? (first results))
			(= (count results) 1)))
	 (let [new-indicators 
	       (for+ [indicator (vals i) result results]
		     (merge indicator
			    {:vec
			     (aif result 
				  it
				  (do
				    (info "mql error %s when sending indicator %s" it (dissoc indicator :vec)) 
				    (:vec indicator)))
			     :from now}))]  
	   (reset! *indicators*
		   (zipmap (keys i) new-indicators))
	   (count new-indicators))
	 (first results)))))         

(defonce *global-indicator-update-pid* "")
(defvar indicator-service-sleep-time 1000)
(defn alive? []
  (pid? *global-indicator-update-pid*))
(defn stop []
  (if (alive?)
    (! *global-indicator-update-pid* "STOP")
    (warn "global indicator update service is already dead")))
 
;;TODO: when we have clj-forex error, we need to subclass it!
(defn start [] 
  (if (alive?)
    (warn "global indicator update service is already alive")
    (debugging
     "Global Indicator Update Service"
     (var-root-set
      #'*global-indicator-update-pid*
      (spawn-log
       #(loop []
	  (try
	    (clean-rates)
	    (refresh-rates) 
	    (catch Exception e
	      (severe e) 
	      (.printStackTrace e)))
	  (Thread/sleep indicator-service-sleep-time)
	  (if (= (? 0) "STOP")
	    (info "stopping...")
	    (recur)))
       "Global Indicator Update Service"))))) 