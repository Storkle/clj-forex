;;forex.backend.common - run a backend and export necessary functions for the user
 
(ns forex.backend.common
  (:use utils.general emacs forex.util.log)
  (:require 
   [forex.backend.common.core :as core]
   [forex.backend.common.service :as service]
   [forex.backend.mql.common :as mql]))
   
(comment

(defvar backend-type :mql 
  "Default backend used")
(defvar backend-start-after-hook '()
  "Functions to run after backend has started")
(defvar backend-stop-after-hook '()
  "Functions to run after backend has started ")

;;TODO: better composability of background services
(defn start []
  (cond 
    (= backend-type :mql)
    (let [m (mql/new-mql)]
      (core/start m nil)
      (var-root-set #'core/*backend* m))
    true (throwf "invalid backend %s" type))
  (run-hooks backend-start-after-hook)
  true)
(defn alive? []
  (if-not core/*backend*
    false
    (core/alive? core/*backend*)))
 
(defn stop []
  (is core/*backend* "no backend current running...")
  (core/stop core/*backend* nil)
  (var-root-set #'core/*backend* nil)
  (run-hooks backend-stop-after-hook)
  true)
 
;;exporting the following:
;;start/stop/get-stream/refresh-rates/context
;;backend-type/backend-start-after-hook/backend-stop-after-hook

(def get-stream service/get-stream)
(def refresh-rates service/refresh-rates)
(defmacro context [& body] `(service/context ~@body))

)


