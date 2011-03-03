(ns forex.module.service
  (:require
   clojure.contrib.core
   [forex.backend.mql.socket-service :as backend]
   [forex.module.indicator.service :as indicator])) 

;;TODO: synchronizing between starts
(defn start [] (backend/start) (indicator/start))
(defn stop []  (indicator/stop) (backend/stop))
(defn alive? [] 
  (or (backend/alive?) (indicator/alive?)))
(defn alives? []
  (and (backend/alive?) (indicator/alive?)))
