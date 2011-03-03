
(clojure.core/use 'nstools.ns)
(ns+ forex.util.log
  (:clone clj.core)
  (:import [java.util.logging Logger Level LogManager Handler
            FileHandler SimpleFormatter ConsoleHandler])
  (:require [clojure.contrib.duck-streams :as f])
  (:use forex.util.emacs forex.util.general))

;;TODO: minor mode
(defvar log-dir "%h/.forex")
(defvar log-file)

(defn- formatter [] 
  (let [d (java.util.Date.)]
    (proxy [java.util.logging.Formatter] []
      (format [r] 
              (clojure.core/format "%s%n%s: %s%n%n"
                                   (do (.setTime d (.getMillis r)) d)
                                   (.getLevel r)
                                   (.getMessage r))))))

;;wrap the PrintWriter *out* in an OutputStream to be used in ConsoleHandler
(comment
  (defn- new-out-stream [out]
    (proxy [java.io.OutputStream] []
      (close [] (.close out))
      (flush [] (.flush out))
      (write ([b] (.print out (String. b)))
             ([b off len] (.print out (String. b off len)))))))

(defn new-logger
  ([file] (new-logger file true))
  ([file make-file]
     (when make-file
       (let [file (format "%s/.forex/%s" (System/getProperty "user.home")
			  file)]
	 (f/make-parents
	  (java.io.File. file))
	 (var-root-set #'log-file file)))
     (let [l (java.util.logging.Logger/getLogger (str *ns*))]
       (mapc #(.removeHandler l %) (.getHandlers l))
       (.addHandler l (doto (ConsoleHandler.) (.setFormatter (formatter))))
       (when make-file
         (.addHandler l (doto (FileHandler. (str log-dir "/" file))
                          (.setFormatter (formatter)))))
       (.setUseParentHandlers l false)
       l)))

(defvar log (new-logger "forex-log" false))

;;TODO: set filtering levels
;;fine,finer,finest wont log

(def- *debug-info* "")
(defmacro debugging [str & args] `(binding [*debug-info* ~str] ~@args))

(defn info [msg & args]
  (.info log (apply format (str *debug-info* " " msg) args)))
(defn out [msg & args]
  (println (apply format (str "INFO: " *debug-info* " " msg) args))
  (apply info msg args)
  nil)

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

