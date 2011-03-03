
(clojure.core/use 'nstools.ns)
(ns+ forex.util.core
  (:clone clj.core)
  (:use forex.util.emacs forex.util.general forex.util.spawn forex.util.log)
  (:require clojure.contrib.error-kit clojure.contrib.pprint)
  (:import java.io.File (org.joda.time DateTime DateTimeZone Instant)))

(defonce *debug* true)
(defn debug [msg & args]
  (when *debug* (apply out (str "DEBUG: " msg) args)))
;;TODO: we need a combination of saving using byte straems and saving as clojure! that way clojure stuff is compatible with later stuff 

(defn- make-exception [a] (Exception. a))
(defmethod print-dup Exception [o w]
  (.write w (str "#=(forex.util.core/make-exception \"" (.getMessage o) "\")")))

;;TODO: memoize pow?
;;used from http://groups.google.com/group/clojure/browse_thread/thread/cb5246d07142a3dc?fwc=2&pli=1
(defn frm-save 
 "Save a clojure form to file." 
  [file form] 
  (with-open [w (java.io.FileWriter.
                 (if (instance? File file) file (File. file)))] 
    (binding [*out* w *print-dup* true] (prn form))))

(defn frm-load 
  "Load a clojure form from file." 
  [file] 
  (with-open [r (java.io.PushbackReader. (java.io.FileReader. (if (instance? File file) file (File. file))))] 
    (read r)))

(defn round [num places]
  (let [multiplier (Math/pow 10 places)]
    (/ (int (* num multiplier)) multiplier)))
(defn ns? [a] (instance? clojure.lang.Namespace a))
(defn decimal-places [num]
  (let [^String s (reverse (second (.split (str (double num)) "\\.")))
        c (count s)]
    (loop [c (nth s 0) i 0]
      (cond
       (or (= i c) (not (= c \0)))
       (- (count s) i)
       true
       (recur (nth s (inc i)) (inc i))))))

(defn ns-symbol
  ([symbol] (ns-symbol symbol *ns*))
  ([symbol ns] (ns-symbol symbol ns nil))
  ([symbol ns default]
     (let [var ((ns-interns ns) symbol)]
       (if (and (var? var) (var-get var)) var default))))

(deftype AtomHash [val]
  Object
  (toString [this] (str "<AtomHash " @val ">"))
  clojure.lang.IPersistentMap
  ;;ILookup
  (valAt [this key] (get @val key))
  (valAt [this key notfound] (get @val key notfound))
  ;;IPersistentCollection
  (count [this] (.count @val))
  (empty [this]  {}) 
  (cons [this e]  (.cons @val e))
  (equiv [this gs] (or (identical? this gs)
                       (when (identical? (class this) (class gs))
                         (= val (.val gs)))))
  (containsKey [this k] (or (and (get @val k) true) false))
  (entryAt [this k] (get @val k))
  ;;Seqable
  (seq [this] (seq @val))
  ;;Associative 
  (assoc [this k g] (assoc @val k g))
  (assocEx [this k g] (assoc this k g))
  (without [this k] (.without @val k))
  clojure.lang.IDeref
  (deref [this] @val))
;;todo - create ns-resolve-symbol, instead of hardcoding namespace
;;TODO: add pprint
(defmethod print-dup AtomHash [o w]
  (.write w "#=(forex.util.core/atom-hash ") (print-dup @o w) (.write w ")"))

(defmethod clojure.core/print-method AtomHash [o w]
  (.write w (.toString o)))
;;TODO: make into protocol method! not multimethod!
(defmethod swap!  forex.util.core.AtomHash [a & args]
  (apply swap! (.val a) args))
(defmethod reset!  forex.util.core.AtomHash [a & args]
  (apply reset! (.val a) args))

(defn atom-hash
  ([] (atom-hash {}))
  ([val]
     (let [val (if (nil? val) {} val)]
      (is (map? val))
      (AtomHash. (atom val)))))

(defn symbolicate
  "symbolicate symbols together. ignores things like whitespaces, just drops them!"
  [& args]
  (symbol (apply str args)))

(defmacro naive-var-local-cache-strategy [var] 
 `(let [cache# (atom {})]
    (reify PCachingStrategy
      (retrieve [_ item#] (get @cache# item#))
      (cached? [_ item#] (contains? @cache# item#))
      (hit [this# _] this#)
      (miss [this# item# result#]
            (reset! cache# (swap! ~var assoc item# result#))
            this#))))

(defmacro constants [& args]
  `(do ~@(map (fn [[name val]] `(def ~name ~val)) (group args 2))))

(defmacro spawn-log [func name]
  `(spawn (fn [] (try (~func) (catch Exception e#
                                (.printStackTrace e#) (severe e#))))
          ~name)) 

(defonce *env* (atom {:period 240 :symbol "EURUSD"})) ;default +H4+, EURUSD
(defn env
  ([] @*env*)
  ([key] (get @*env* key)))
(defn env! [map] 
  (swap! *env* #(merge % map))
  map)

(defmacro wenv [map & body] 
  `(binding [forex.util.core/*env*
             (atom (merge @@~#'*env* ~map))]
     ~@body))

;;aliases for error kit
(defn ns-export [from-ns]  
  (count (doall (map (fn [[sym var]]
                       (let [var-obj (if (.hasRoot var)
                                       (intern *ns* sym (var-get var))
                                       (intern *ns* sym))]
                         (when var-obj
                           (alter-meta! var-obj
                                        (fn [old] (merge (meta var) old)))
                           var-obj)))
                     (ns-publics from-ns)))))

(defmacro eval-when [& args]
  (eval `(do ~@args)) nil)

(eval-when
 (require 'clojure.contrib.error-kit)
 (ns-export (find-ns 'clojure.contrib.error-kit)))

(deferror *clj-forex-error* [] [message] 
  {:msg (str "clj-forex error: " message) 
   :unhandled (throw-msg Exception)})

(defn throwf [msg & args] (raise *clj-forex-error*
				 (apply format (str msg) args)))

(defmacro is?
  [val & message]
  (if (vector? val)
    `(do ~@(map (fn [test] `(is? ~test ~@message)) val))
    `(let [result# ~val]
       (if (not result#)
         (throwf  ~(or (and (first message)
                            `(format ~@message))
                       (format "assertion %s failed"
                               (str val))))
         result#))))

