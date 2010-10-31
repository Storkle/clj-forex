(ns forex.ea (:use forex.utils forex.binding))
;;NOTE: is prototype
;;TODO: make convenient macros for ea function; do on bar, intra bar, etc.
;;TODO: integrate mmemail to send email alerts
;;TODO: make debugging simple: statuses, etc.
					;TODO: multiple connections, and this means no indicator serving and that it automatically manages any scalping trades

(defonce *ea* (atom {}))
(defn bar-id [] (open))

(defn add-ea [type {symbol :symbol timeframe :timeframe} function]
  (let [prev (get @*ea* type)
	thread (thread (wenv (:symbol symbol :timeframe timeframe) (function)))
	ea-id {:id (gensym) :type type :thread thread :symbol symbol :timeframe timeframe}]
    (swap! *ea* assoc type (concat prev (list ea-id)))
    ea-id))


					;usage
(comment (def ea (add-ea :impulse {:symbol "EURUSD" :timeframe +H1+} run-impulse))
	 (defn run-impulse []
	   (loop []
	     (Thread/sleep 3000)
	     (do-something)
	     (recur))))
