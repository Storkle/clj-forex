(clojure.core/use 'nstools.ns)
(ns+ forex.default
     (:clone clj.core)
     (:use forex.util.general)
     (:use forex.util.core
           forex.util.emacs
           forex.util.log)  
     (:use forex.module.error
           forex.module.ea
	   ;;TODO: change indicator, get indicator service?
           [forex.module.indicator :exclude [start stop alive?]]
           forex.module.account forex.module.account.utils
           [clj-time.core :exclude [extend start]])
     (:require
      [forex.module.service :as backend]))


