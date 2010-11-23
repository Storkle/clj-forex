(ns forex.indicators)
(in-ns 'forex.indicators)
(use 'forex.socket)
(use 'forex.utils)
(import '(org.joda.time Instant DateTime))
(import '(org.joda.time Instant DateTime DateTimeZone Interval))
(defn now [] (DateTime. DateTimeZone/UTC))
(defn abs-time
  ([] (int (/ (.getMillis (Instant. (now))) 1000)))
  ([date] (int (/ (.getMillis (Instant. date)) 1000))))

(defn get-abs-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is  (> to from) "in get-data, from/to is wrong")
  (write-stream (env :socket) (str "barss " from " " to)) 
  (write-stream (env :socket) (str symbol " " timeframe))
  (receive-stream (env :socket)))
(defn get-rel-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is  (> to from) "in get-data, from/to is wrong")
  (write-stream (env :socket) (str "bars " to " " from)) 
  (write-stream (env :socket) (str symbol " " timeframe))
  (receive-stream (env :socket)))

(def val (get-rel-data "EURUSD" 60 0 100))
(def val (get-abs-data "EURUSD" 60 (abs-time (.minusDays (now) 3)) (abs-time (now)) ))
					; start-date count data0 ......
					;bar bardate



;;;;indicator test!!!!
(import 'forex.indicator.core.ForexStream)
(def stream (ForexStream. "EURUSD" 60))
(defn out [s] (println (str "[" s "]")))
(init-stream stream)

;;todo: if we get an error! - do not update?
;;update data until theres NO ERRORS!
(let [dat (loop [prev-data {}
		 streams nil retries 0]
	    (if (> retries 3) (throwf "error! cannot update all data at once"))
	    (let [d (map get-data streams)
		  errors (map get-errors d)]
	      (if errors
		(recur (merge prev-data d)
		       errors (+ retries 1))
		(merge prev-data d))))]
  (map #(place struct data) dat))




(defn update-stream [stream]
  (let [data (get-abs-data (.symbol stream) (.timeframe stream) (.getHead stream) (abs-time (now)))]
    (pr (second (rest data)))
    (doseq* [[hi lo open close] (group (rest (rest data)) 4)
	     i (iterate inc 0)]
      (if (= i 0)
	(.put stream (- (.size stream) 1) (Double/parseDouble hi) (Double/parseDouble lo) (Double/parseDouble open) (Double/parseDouble close))
	(.add stream hi lo open close)))
    (.setHead stream (Integer/parseInt (first data)))))


(defn init-stream
  ([stream] (init-stream stream 500))
  ([stream amount]
     (is (<= amount 1000) "cant go above 1000 bar storage for now")
     (out (format "initializing stream: %s %s" (.symbol stream) (.timeframe stream)))
     (let [data (get-rel-data (.symbol stream) (.timeframe stream) 0 amount)]
       (out "done")
       (doseq* [[hi lo open close] (reverse (group (rest (rest data)) 4)) i (iterate inc 0)]
	 (.put stream
	       i
	       (Double/parseDouble hi)
	       (Double/parseDouble lo) (Double/parseDouble open)
	       (Double/parseDouble close))
	 (.setHead stream (Integer/parseInt (first data)))))
     stream))