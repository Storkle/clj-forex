
(ns forex.util.general 
  (:require  [clojure.contrib.str-utils2 :as s] clojure.string)
  (:import java.lang.management.ManagementFactory)
  (:require [matchure :as m]
            [clojure.contrib.def :as d])
  (:use clojure.contrib.macro-utils))

(defmacro for+ [args & body]
  (let [a (partition-all 2 args)]
    `(map (fn ~(vec (map first a)) ~@body) ~@(map second a))))

(defmacro with-out-str+
  "Evaluates exprs in a context in which *out* is bound to a fresh
  StringWriter.  Returns the string created by any nested printing
  calls."  {:added "1.0"} [arg & body]
  `(let [~arg (new java.io.StringWriter)]
     (binding [*out* ~arg]
       ~@body)))

(defn add-shutdown-hook [a]
  (try
    (.addShutdownHook (Runtime/getRuntime) a)
    true
    (catch Exception e false)))

(defn dump-threads []
  (let [threads (.dumpAllThreads (ManagementFactory/getThreadMXBean) false false)
	stacks (map #(.getStackTrace %) threads)]
   (doall
    (map (fn [stack thread]
	   (println (.getThreadName thread))
	   (doall (map println stack)) (println))
	 stacks threads))
   (count stacks)))


(defn seq1 [s]
  (reify clojure.lang.ISeq
    (first [_] (.first s))
    (more [_] (seq1 (.more s)))
    (next [_] (let [sn (.next s)] (and sn (seq1 sn))))
    (seq [_] (let [ss (.seq s)] (and ss (seq1 ss))))
    (count [_] (.count s))
    (cons [_ o] (.cons s o))
    (empty [_] (.empty s))
    (equiv [_ o] (.equiv s o))))
(d/defalias defalias d/defalias)

(defn upper? [s]
  (= (.toUpperCase s) s))

(def join clojure.string/join)
(def split s/split)
(defn atom? [a] (or (symbol? a) (number? a)))

;;camel case - gotten from defrecord2 on github
(defn- assemble-words [parts]
  (loop [remaining-parts parts result []]
    (if (seq remaining-parts)
      (let [part (first remaining-parts)]
        (recur (rest remaining-parts)
               (if (upper? part)
                 (conj result (.toLowerCase part))
                 (conj (if (seq result)
                         (pop result)
                         []) (str (last result) part)))))
      result)))

(defn camel-to-dash
  "Convert a name like 'BigBlueCar' to 'big-blue-car'."
  [s]
  (let [parts (remove #(= "" %) (s/partition s #"[A-Z]"))
        words (assemble-words parts)]
    (join "-" words)))
;;;

(defn classpath []
  (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))

(defn sequence? [a] (or (list? a) (vector? a) (seq? a)))
(defn var-root-set [var val]
  (alter-var-root var (constantly val)))

;;copied from  
(defprotocol PCachingStrategy
  "A caching strategy implements the backend for memoize. It handles the
  underlying cache and might define different strategies to remove old
  items from the cache."
  (retrieve [cache item] "Get the requested cache item.")
  (cached?  [cache item] "Checks whether the given argument list is cached.")
  (hit      [cache item] "Called in case of a cache hit.")
  (miss     [cache item result] "Called in case of a cache miss."))

(declare naive-cache-strategy)

(defn mem
  "Returns a memoized version of a referentially transparent function.
  The memoized version of the function keeps a cache of the mapping from
  arguments to results and, when calls with the same arguments are repeated
  often, has higher performance at the expense of higher memory use.
  Optionally takes a cache strategy. Default is the naive safe all strategy."
  ([f] (mem f (naive-cache-strategy)))
  ([f strategy]
   (let [cache-state (atom strategy)
         hit-or-miss (fn [cache item]
                       (if (cached? cache item)
                         (hit cache item)
                         (miss cache item (delay (apply f item)))))]
     (fn [& args]
       (let [cs (swap! cache-state hit-or-miss args)]
         @(retrieve cs args))))))

(deftype ^{:private true} NaiveStrategy [cache]
  PCachingStrategy
  (retrieve
    [_ item]
    (get cache item))
  (cached?
    [_ item]
    (contains? cache item))
  (hit
    [this _]
    this)
  (miss
    [_ item result]
    (NaiveStrategy. (assoc cache item result))))

(defn- naive-cache-strategy
  "The naive safe-all cache strategy for memoize."
  []
  (NaiveStrategy. {}))  2



;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;

(defmacro defonce-
  "Same as defonce but yields a private definition"
  ([name expr]
     (list `defonce (with-meta name (assoc (meta name) :private true)) expr))
  ([name expr doc]
     (list `defonce (with-meta name (assoc (meta name) :private true :doc doc)) expr)))
(defmacro def-
  "Same as def but yields a private definition"
  [name & decls]
  (list* `def (with-meta name (assoc (meta name) :private true)) decls))
(defmacro defmacro-
  "Same as defmacro but yields a private definition"
  [name & decls]
  (list* `defmacro (with-meta name (assoc (meta name) :private true)) decls))
;;WARNING: this can screw up protocols implemented on existing object - i.e. replace them, they no longer work! so you have to create new object. how to fix this???...
(defn reload
  ([] (reload *ns*))
  ([n]
     (let [name (cond (symbol? n) n true (ns-name n))]
       (require name :reload-all))))

(def- *fake* (gensym))
(defmacro defrecord+ 
  [record-name fields-and-values constructor-name & record-body] 
  (let [fields-and-values (map #(if (vector? %) % [% nil])
                               fields-and-values) 
        fields            (vec (map first fields-and-values)) 
        default-map       (into {} fields-and-values)
        fn-name (symbol (or (and constructor-name (str constructor-name))
                            (str "new-" (name record-name))))] 
    `(do 
       (defrecord ~record-name 
           ~fields 
         ~@record-body) 
       (defn ~fn-name
         ([] (~fn-name ~#'*fake* nil))
         ([& {:keys ~fields :or ~default-map}] 
            (new ~record-name ~@fields))))))

(binding [*out* *out*] (defn- log [e] (.println *out* (str "ERROR!: " e))))

;;TODO: get rid of!
(defmacro mapc [& args] `(dorun (map ~@args)))
(defmacro thread [& body]
  `(let [thread# (Thread.
                  (bound-fn [] (try (do ~@body) (catch Exception e# (println "error in thread " e#)))))]
     (.start thread#)
     thread#))

(defmacro is [val & message]
  `(let [result# ~val]
     (if (not result#)
       (throw (Exception. ~(or (and (first message) `(format ~@message)) (format "assert: %s" (str val)))))
       result#)))
;;TODO: efficiency: timeout without throwing exception? and definitely a timeout with all that thread hastle!
;; this should be done in a thread pool!
(defmacro pf [& args]
  `(print (format ~@args)))

(comment
  (defmacro throwf [message & args]
   (if args
     `(throw (Exception. (format ~message ~@args)))
     `(throw (Exception. ~message)))))

(defn group
  ([coll] (group coll 2))
  ([coll by] (partition-all by coll)))

(defmacro do1 [a & body]
  `(let [ret# ~a]
     ~@body
     ret#))
(defmacro mapc [& args] `(dorun (map ~@args)))
(defn sleep [s] (Thread/sleep (* 1000 s)))


(defmacro on [[& args] & body]
  (let [a (group args 2)
        first-args (map first a)
        second-args (map second a)]
    `(doseq [[~@first-args] (map vector ~@second-args)]
       ~@body)))





(defalias if-match m/if-match)
(defalias when-match m/when-match)
(defalias cond-match m/cond-match)
(defmacro match
  "match item with caluses"
  [item & clauses] 
  (let [msg-gen (gensym)]
    `(let [~msg-gen ~item] 
       (cond-match
        ~@(mapcat (fn [[test body]]
                    `[[~test ~msg-gen] ~body]) (group clauses 2))))))


;;TAKEN FROM debug-repl
(defmacro locals
  "Produces a map of the names of local bindings to their values."
  []
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym)) symbols) symbols)))


