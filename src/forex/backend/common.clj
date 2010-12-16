(ns forex.backend.common
  (:use utils.general emacs)
  (:require
   [forex.backend.common.core :as core]
   [forex.backend.common.mql :as mql]
   [forex.backend.common.service :as service]))
 
(defvar backend-type :mql
  "Default backend used")
 
(defn start []
  (cond
    (= backend-type :mql) (do (alter-var-root  #'core/*backend* (mql/new-mql))
			      (core/start core/*backend* nil))
    true (throwf "invalid backend %s" type)))
  
(defn stop []
  (is core/*backend* "no backend current running...")
  (let [prev core/*backend*] 
   (core/stop core/*backend* nil)
   (alter-var-root #'core/*backend* nil)
   prev))

;;exporting!!!
;;start/stop/backend-type
;;get-stream/refresh-rates/context/indicator-naive-cache-strategy

(def get-stream service/get-stream)
(def refresh-rates service/refresh-rates)
(def indicator-naive-cache-strategy service/indicator-naive-cache-strategy)
(defmacro context [& body] `(service/context ~@body))




