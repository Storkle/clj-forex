;;forex.module.order.common - api for creating/modify orders

(ns forex.module.order.common
  (:use utils.general emacs 
	forex.util.general
	forex.module.order.order
	forex.backend.mql.socket_service)
  (:require [forex.backend.common :as backend]
	    [forex.indicator.common :as ind]))

(comment
  (defn modify [o args] (swap! merge-map (:gen-map args)))
  (defn generate [o]
    (let [diff (map-difference (:orig-map o) (:gen-map o))]
      (when (or (:tp diff) (:sl diff)) (change-order-sl o :sl (:sl diff) :tp (:tp diff)))
      (awhen (:lots diff) (change-lots o it))
      true))) 

;;; api  
;;create order - (new-order {:type "buystop" :sl sl :tp tp})
;;modify order - (modify order {:sl sl :tp tp})
;;update order - (.generate order) 
