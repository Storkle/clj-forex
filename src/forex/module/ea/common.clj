(ns forex.module.ea.common
  (:use forex.indicator.common
	forex.util.general
	utils.general))

(defonce- *ea* (atom {}))

(defn bar-id [] (open))

(defn add-ea [type {symbol :symbol timeframe :timeframe} function]
  (let [prev (get @*ea* type)
	pid  (spawn-log
		 (wenv (:symbol symbol :timeframe timeframe)
		   (function)))
	ea-id {:id (gensym) :type type :pid pid
	       :symbol symbol
	       :timeframe timeframe}]
    (swap! *ea* #(assoc % type (concat prev (list ea-id))))
    ea-id)) 

(comment
  (def ea (add-ea :impulse {:symbol "EURUSD" :timeframe +H1+} run-impulse))
  (defn run-impulse []
    (loop []
      (Thread/sleep 3000)
      (do-something)
      (recur))))
