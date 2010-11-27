(ns forex.indicator_core
  (:refer-clojure :exclude (=))
  (:use forex.utils forex.binding))

				
(defonce- *mem* (atom {})) ;TODO: prevent evil printing of indicators
					; below taken from http://kotka.de/blog/2010/03/memoize_done_right.html

(defn update-all-indicators []
  (on [ind (vals @*mem*) key (keys @*mem*)]
    (try
      (.update (force ind))
      (catch Exception e
	(print "removing indicator")
	(= *mem* (dissoc % key)))))
  true)
 
(defn memoi
  ([f] (memoi f [{} identity (fn [mem args] mem) assoc]))
  ([f [init cached hit assoc]]
     (let [mem         *mem* 
	   hit-or-assoc (fn [mem args]
			  (let [a (rest args)]
			    (if (contains? (cached mem) a)
			      (hit mem a)
			      (assoc mem a (delay (apply f args))))))]
       (fn [& args]
	 (let [m (swap! mem hit-or-assoc args)] 
	      (-> (cached m) (get (rest args)) deref)))))) 


(defn _get-indicator [f ind-name symbol timeframe params]
  (let [ind (f params)] 
       (.update ind) ;TODO: this needs to lock indicator? write lock?
    ind))
(def get-indicator (memoi _get-indicator))
 
(defn substream [stream price]
  (cond
    (is price :close) (.Close stream)
    (is price :open) (.Open stream)
    (is price :high) (.High stream)
    (is price :low) (.Low stream)
    true price))

(defn env-stream []
  (let [stream (get-stream (env :symbol) (env :timeframe))]
    (env! {:stream stream})
    stream))

 
					;copy and pasted for various parameter situation - ya
;;todo: make better and dont hash until you update it?
(defn indicator
  ([id f] (indicator id f (fn [obj index] (.get obj index))))
  ([id f getfn]
   (fn ind
     ([ps] (ind ps 0))
     ([ps index] (ind ps index {}))
     ([ps index {:keys [stream] :or {stream (env-stream)}}]
	(let [params (concat [stream] ps)
	      symbol (env :symbol)
	      timeframe (env :timeframe)
	      indicator (get-indicator f id symbol timeframe params)
	      get-it (fn [index] (.get indicator index))] 
	  (if (is index nil)
	    (partial getfn indicator)
	    (getfn indicator index)))))))

(defn indicator1
  ([id f] (indicator1 id f (fn [obj index] (.get obj index))))
  ([id f getfn]
   (fn ind
     ([ps] (ind ps 0))
     ([ps index] (ind ps index {}))
     ([ps index {:keys [stream] :or {stream (env-stream)}}] 
	(let [params (concat [stream] [ps])
	      symbol (env :symbol)
	      timeframe (env :timeframe)
	      indicator (get-indicator f id symbol timeframe params)
	      get-it (fn [index] (.get indicator index))] 
	  (if (is index nil)
	    (partial getfn indicator)
	    (getfn indicator index)))))))

(defn price-indicator1
  ([id f] (price-indicator1 id f (fn [obj index] (.get obj index))))
  ([id f getfn]
   (fn ind
     ([ps] (ind ps 0))
     ([ps index] (ind ps index {}))
     ([ps index {:keys [price stream] :or {stream (env-stream) price :close}}]
	(let [params (concat [stream (substream stream price)] [ps])
	      symbol (env :symbol)
	      timeframe (env :timeframe)
	      indicator (get-indicator f id symbol timeframe params)
	      get-it (fn [index] (.get indicator index))] 
	  (if (is index nil)
	    (partial getfn indicator)
	    (getfn indicator index)))))))

(defn price-indicator
  ([id f] (price-indicator id f (fn [obj index] (.get obj index))))
  ([id f getfn]
   (fn ind
     ([ps] (ind ps 0))
     ([ps index] (ind ps index {}))
     ([ps index {:keys [price stream] :or {stream (env-stream) price :close}}]
	(let [params (concat [stream (substream stream price)] ps)
	      symbol (env :symbol)
	      timeframe (env :timeframe)
	      indicator (get-indicator f id symbol timeframe params)
	      get-it (fn [index] (.get indicator index))] 
	  (if (is index nil)
	    (partial getfn indicator)
	    (getfn indicator index)))))))


;;TODO: remove from cache
 					
(def *global-update-thread* nil)
;;TODO: request backend to stop after making thread safe!
(defn stop-backend [] (.stop *global-update-thread*)
  (def *global-update-thread* nil))
(defn backend-alive? []
  (.isAlive *global-update-thread*))
(defn start-backend []
  (def *global-update-thread*
    (thread
      (loop []
	(try    
	 (update-streams @forex.binding/*streams*)
	 (forex.indicator_core/update-all-indicators) 
	 (sleep 1000)	 
	 (catch Exception e
	   (log (str "CAUGHT GLOBAL UPDATE THREAD ERROR: " e))
	   (sleep 1000)))
	(recur))))) 
