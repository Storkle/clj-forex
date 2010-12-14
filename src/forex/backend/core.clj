(ns forex.backend.core
  (:use utils.general)
  (:require [ forex.backend.mql :as mql]))

(defonce- *backend* nil) 
(def *default-backend* :mql)

;;TODO: somehow lock for mql we must pause????
(defn start-backend
  ([] (start-backend *default-backend*))
  ([type]
     ;;(init-logger) 
     (cond
       (= type :mql) (do (def *backend* (mql/new-mql))
			 (mql/start *backend* nil))
       true (throwf "invalid backend %s" type))))
 
(defn stop-backend []
  (is *backend* "no backend current running...")
  (let [prev *backend*] 
   (mql/stop *backend* nil)
   (def *backend* nil)
   prev))  
  
(defn get-stream [symbol timeframe]
  (is *backend* "no backend current running...")
  (is (and (string? symbol) (integer? timeframe))
      "invalid params in get-price-stream: %s ,%s" symbol timeframe)
  (mql/get-price-stream *backend* symbol timeframe))




 