
(clojure.core/use 'nstools.ns)
(ns+ forex.module.error
     (:clone clj.core)
     (:require forex.backend.mql.socket-service)
     (:require forex.backend.mql.error)
     (:require [forex.backend.mql.socket-service :as s])
     (:use forex.util.general
	   forex.util.core
	   forex.util.emacs 
	   forex.util.general))

(def e? forex.backend.mql.error/e?)
(def new-mql-error forex.backend.mql.error/new-mql-error)
(defmacro aif
  ([test then] `(aif ~test ~then nil))
  ([test then else]
     `(let [~'it ~test] 
        (if (and ~'it (not (e? ~'it)))
          ~then
          ~else)))) 
(defmacro awhen [test & body] `(aif ~test (do ~@body)))
(defmacro aif-not 
  ([test then] `(aif-not ~test ~then nil))
  ([test then else]
     `(let [~'it ~test]
        (if (not (and ~'it (not (e? ~'it))))
          ~then
          ~else)))) 
(defmacro awhen-not [test & body] `(aif-not ~test (do ~@body)))

;;receive with errors
(def receive-lst s/receive)
 
(defn receive 
  ([msg] (receive msg 3))
  ([msg resend] (receive msg resend false))
  ([msg resend wait-on] (receive msg resend wait-on false))
  ([msg resend wait-on try]
     (loop [retries 0] 
       (let [result  (first (receive-lst msg resend))]
	 (if (e? result)
	   (let [e (:e result)] 
	     (cond 
	      (and try (< retries try)
		   (or (= e 4066) (= e 4054)))
	      (do (Thread/sleep 300) (recur (inc retries)))
	      true result))
	   result)))))

(defn receive-indicator
  ([msg] (receive-indicator 3))
  ([msg retries]
     (receive msg false false retries)))
(defn receive-critical [msg]
  (receive msg false true))

(defn receive! [& args]
  (aif (apply receive args)
       it
       (throwf "MQL error %s when sending message with args %s" it args)))  


