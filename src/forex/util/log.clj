;;forex.util.log - log to a log file and to *out* and System/out

(ns forex.util.log
  (:import [java.util.logging Logger Level LogManager Handler
	    FileHandler SimpleFormatter ConsoleHandler])
  (:require [clojure.contrib.duck-streams :as f])
  (:use emacs utils.general))

;;TODO: minor mode
(defvar log-dir "%h/.forex"
  "Directory of logging")

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
;;this will create a logger which logs to /home/dir/.forex/log.log and will output to System/out (in emacs+slime, this is in the *inferior-lisp* buffer
;;or in the *shell* if you do lein swank

(defn- new-logger [file]
  (f/make-parents
   (java.io.File. (format "%s/.forex/%s" (System/getProperty "user.home") file)))
  (let [l (java.util.logging.Logger/getLogger (str *ns*))]
    (mapc #(.removeHandler l %) (.getHandlers l))
    (.addHandler l (doto (ConsoleHandler.) (.setFormatter (formatter))) ;;(new-out-stream *out*) = to *out*, but sort of clutters everything
		 )
    (.addHandler l (doto (FileHandler. (str log-dir "/" file))
		     (.setFormatter (formatter))))
    (.setUseParentHandlers l false)
    l))

(defonce- log (java.util.logging.Logger/getLogger (str *ns*)))

;;TODO: only use one log file!!! eh?
(defn init-logger []
  (if-not log
   (def- log (new-logger "log"))))
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
  (let [s (apply format (str *debug-info* " " msg) args)]
    (.severe log s)
    (print (format "SEVERE: %s%n" s))))
(defn warn [msg & args]
  (let [s (apply format (str *debug-info* " " msg) args)]
    (.warning log s)
    (print (format "WARNING: %s%n" s)))) 
