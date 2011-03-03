(clojure.core/use 'nstools.ns)
(ns+ forex.module.indicator.shorthand
     (:clone clj.core)
     (:use forex.module.indicator.map
	   forex.module.indicator.util)
     (:use forex.util.emacs
	   [clj-time.core :exclude [extend start]]
	   [clj-time.coerce]
	   forex.util.spawn
	   forex.util.core
	   forex.util.log
	   forex.util.general
	   forex.module.account.utils 
	   forex.module.error)   
     (:require clojure.contrib.core
      [forex.backend.mql.socket-service :as backend]))

 
(defn- fn-args [param? mode?]
  (cond
   (and param? mode?) `([param mode] [param mode index] [param mode index & e])
   (and param? (not mode?)) `([param] [param index] [param index & e])
   mode? `([mode] [mode index] [modex index & e])
   true `([] [index] [index & e])))
(defn- ifn-args [name body param? mode?]
  (cond
   (and param? mode?) `(([param# mode#] (~name param# mode# @*env*))
			([param# mode# & e#] ~@body))
   (and param? (not mode?)) `(([param#])
			      ([param# & e#] ~@body))
   mode? `([mode] [mode & e])
   true `([] [& e])))
;;TODO: defn-arglist which removes ___ from arglist metadata
(defmacro def-indicator [[fn-name mql4-name ifn-name] & [param? mode?]]
  (let [ifn-name (if ifn-name ifn-name (symbolicate "i" fn-name))]
    `(do
       (defn ~ifn-name
	 ~@(cond
	    (and param? mode?)
	    `(([param# mode#] (~ifn-name param# mode# @*env*))
	      ([param# mode# & e#]
		 (wenv (env-dispatch e#)
		       (indicator-vector-memoize
			{:name ~mql4-name
			 :param param#
			 :mode mode#}))))
	    (and param? (not mode?))
	    `(([param#] (~ifn-name param# @*env*))
	      ([param# & e#]
		 (wenv (env-dispatch e#)
		       (indicator-vector-memoize {:name ~mql4-name
						  :param param#}))))
	    mode?
	    `(([mode#] (~ifn-name mode# @*env*))
	      ([mode# & e#]
		 (wenv (env-dispatch e#)
		       (indicator-vector-memoize
			{:name ~mql4-name :mode mode#}))))
	    true `(([] (~ifn-name @*env*))
		   ([& e#]
		      (wenv (env-dispatch e#)
			    (indicator-vector-memoize {:name ~mql4-name}))))))
       (defn ~fn-name
	 ~@(cond
	    (and param? mode?)
	    `(([param# mode#] (~fn-name param# mode# 0))
	      ([param# mode# index#] (~fn-name param# mode# index# @*env*))
	      ([param# mode# index# & e#]
		 (wenv (env-dispatch e#)
		       (indicator-vector1-memoize
			index#
			{:name ~mql4-name
			 :param param#
			 :mode mode#}))))
	    (and param? (not mode?))
	    `(([param#] (~fn-name param# 0))
	      ([param# index#] (~fn-name param# index# @*env*))
	      ([param# index# & e#]
		 (wenv (env-dispatch e#)
		       (indicator-vector1-memoize
			index#
			{:name ~mql4-name
			 :param param#}))))
	    mode?
	    `(([mode#] (~fn-name mode# 0))
	      ([mode# index#] (~fn-name mode# index# @*env*))
	      ([mode# index# & e#]
		 (wenv (env-dispatch e#)
		       (indicator-vector1-memoize
			index#
			{:name ~mql4-name :mode mode#}))))
	    true `(([] (~fn-name 0))
		   ([index#] (~fn-name index# @*env*))
		   ([index# & e#]
		      (wenv (env-dispatch e#)
			    (indicator-vector1-memoize index# {:name ~mql4-name})))))))))




