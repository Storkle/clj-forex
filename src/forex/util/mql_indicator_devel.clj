
(clojure.core/use 'nstools.ns)
;;this file allows one to automatically produce binding code for metatrader custom indicators
(ns+ forex.utils.mql-indicator-devel
     (:clone clj.core)
     (:use forex.util.emacs clj.io)
     (:import java.io.File))
(defvar mql4-indicator-template "src/forex/dev/mql4_indicator_template")
(defvar metatrader-home-dir "/home/seth/.wine/dosdevices/c:/Program Files/FXCM MT4 powered by BT/")

(defmacro for+ [args & body]
  (let [a (partition-all 2 args)]
    `(map (fn ~(vec (map first a)) ~@body) ~@(map second a))))

(defn throwf [msg & args]
  (throw (Exception. (apply format msg args))))

(defn args [s]
  (map rest (re-seq #"extern\s+(\w+)\s+(\w+)" s)))
(defn buffers [s]
  (count (re-seq #"SetIndexBuffer" s)))
(defn extract-args
  "extract the indicator arguments and return as a string"
  [args]
  (let [type-to-fn (fn [type]
                     (condp = type
                         "int" "StrToInteger"
                         "double" "StrToDouble"
                         "string" nil
                         "color" "StrToInteger"
                         "bool" "StrToInteger"
                         (throwf "unknown type %s" type)))
        user-extract (apply str
                            (interpose "\n"
                                       (for+ [[type name] args index (iterate inc 8)]
                                             (let [type-fn (type-to-fn type)]
                                               (if type-fn
                                                 (format "%s %s = %s(request[%s]);" type name type-fn index)
                                                 (format "%s %s = request[%s];" type name index))))))]
   (str user-extract "\n")))  

(defn extension [f] (let [e (second (.split (if (instance? File f) (.getName f) f) "\\."))] (if (string? e) (.trim e))))

(defn to-test
  ([all] (to-test all "process_PERSONAL_INDICATORS"))
  ([all name]
     (format "%nint %s(string c,string command[]) {%n%s%nelse
	     {%nreturn(-1);%n}%nreturn(0);%n}" name
	     (apply str
		    "if (c==\"0\") {\n}\n"
		    (map #(format "else if (c==\"%s\") {%n%s(command);%n}%n" (:function-name %) (:function-name %)) all)
		    )))) 


(defn- listify [s] (if-not (empty? s) (str (apply str (interpose "," s)) ",") "" ))
(defn name-of [f] (first (.split (.getName f) "\\.")))
(defn iCustom
  "given a File, generate mql4 binding code to the indicator in the flie"
  [f prefix]
  (let [name (.replaceAll (first (.split (.getName f) "\\.")) "( |-)+" "_")
        s (slurp f)
        args (map #(list (first %) (format "i_%s" (second %))) (args s))
        buffers (buffers s)]
    (when (and s args buffers)
      (let [extracted-args (extract-args args)
	    custom (format 
		    "iCustom(symbol,timeframe,\"%s\",%smode,i)"
		    (name-of f) (listify (map second args)))
	    function-name (format "%s_%s" prefix name)]  
        {:file f :file-name name
	 :function-name function-name :args args :buffers buffers
         :code  (format (slurp mql4-indicator-template) function-name extracted-args custom)})))) 

(defn iCustom-all [dir out name prefix]
  (let [files (.listFiles (File. dir))
        customs (for [f files :when (= (extension f) "mq4")] (iCustom f prefix))]
    (when (empty? files) (println "WARNING: couldnt find mq4 files for directory " dir))
    (println (format "parsing %s mq4 files ..." (count files)))
    (spit out (format "%s\n%s" (apply str (interpose "\n\n" (map :code customs)))
                      (to-test customs name)))))


;;example usage - first param is custom indicator folder, second is output file
(defn customize-indicators []
  (iCustom-all
   (format "%s/experts/indicators/" metatrader-home-dir)
   (format "%s/experts/include/INDICATORS_DEFAULT.mqh" metatrader-home-dir)
   "process_INDICATORS_DEFAULT"
   "Default")
  (iCustom-all
   (format "%s/experts/indicators/" metatrader-home-dir )
   (format "%s/experts/include/INDICATORS_CUSTOM.mqh" metatrader-home-dir)
   "process_INDICATORS_CUSTOM"
   "Custom")) 


