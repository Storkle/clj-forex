(ns forex.utils (:use utils.general))
(defmacro constants [& args]
  `(do ~@(map (fn [[name val]] `(def ~name ~val)) (group args 2))))

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



