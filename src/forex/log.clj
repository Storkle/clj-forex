(ns forex.log
  (:import [java.util.logging Logger Level LogManager Handler
	    FileHandler SimpleFormatter ConsoleHandler])
  (:require [clojure.contrib.duck-streams :as f])
  (:use utils.general))

(defn- formatter []
  (let [d (java.util.Date.)]
    (proxy [java.util.logging.Formatter] []
      (format [r] 
	      (clojure.core/format "%s%n%s: %s%n%n"
				   (do (.setTime d (.getMillis r)) d)
				   (.getLevel r)
				   (.getMessage r))))))

;;wrap the PrintWriter *out* in an OutputStream to be used in ConsoleHandler
(defn- new-out-stream [out]
  (proxy [java.io.OutputStream] []
    (close [] (.close out))
    (flush [] (.flush out))
    (write ([b] (.print out (String. b)))
	   ([b off len] (.print out (String. b off len))))))

;;TODO: if user deletes log file, it will not be recreated
(defn- new-logger [file]
  (f/make-parents
   (java.io.File. (format "%s/.forex/%s" (System/getProperty "user.home") file)))
  (let [l (java.util.logging.Logger/getLogger (str *ns*))]
    (mapc #(.removeHandler l %) (.getHandlers l))
    (.addHandler l (doto (forex.console. (new-out-stream *out*))
		     (.setFormatter (formatter))))
    (.addHandler l (doto (FileHandler. (str "%h/.forex/" file))
		     (.setFormatter (formatter))))
    (.setUseParentHandlers l false)
    l))

(defonce- log (java.util.logging.Logger/getLogger (str *ns*)))

(defn init-logger []
  (def- log (new-logger "log")))
(init-logger)
;;TODO: set filtering levels
;;fine,finer,finest wont log

(def- *debug-info* "")
(defmacro debugging [str & args] `(binding [*debug-info* ~str] ~@args))

(defn info [msg & args]
   (.info log (apply format (str *debug-info* " " msg) args)))
 (defn fine [msg & args]
   (.fine log (apply format  (str *debug-info* " " msg) args)))
 (defn finer [msg & args]
   (.finer log (apply format (str *debug-info* " " msg) args)))
 (defn finest [msg & args]
   (.finest log (apply format (str *debug-info* " " msg) args)))
 (defn severe [msg & args]
   (.severe log (apply format (str *debug-info* " " msg) args)))
 (defn warn [msg & args]
   (.warning log (apply format (str *debug-info* " " msg) args)))
