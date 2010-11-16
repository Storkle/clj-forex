(ns forex.indicators)
(use 'forex.socket)

;;;;indicator test!!!!
(import 'forex.indicator.core.ForexStream)
(import 'forex.indicator.SMA)
(defn get-data [^String symbol ^Integer timeframe ^Integer from ^Integer to]
  (is (and (<= (- to from) 1000) (> to from)) "from to wrong")
  (write-stream (env :socket) (str "bars " from " " to)) 
  (write-stream (env :socket) (str symbol " " timeframe)) 
  (receive-stream (env :socket)))

(def stream (ForexStream. "EURUSD" 60))
;(connect) 
;(def val1 (get-data "EURUSD" 60 0 1000))
(defn fill-stream [stream]
  (doseq* [[hi lo open close] (reverse (group val1 4)) i (iterate inc 0)]
    (.put stream
	  i
	  (Double/parseDouble hi)
	  (Double/parseDouble lo) (Double/parseDouble open)
	  (Double/parseDouble close))))
;(fill-stream stream)
;(def sma (SMA. stream (.Close stream) 10))

;;todo: dont deinitialize it? ;;todo: make initializing of price stream way faster? with group of course!
 


