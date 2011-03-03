
(ns clj.core
  (:refer-clojure
   :exclude [def promise swap! reset! defrecord spit file])
  (:require clj.third-party.defrecord2)
  (:import clojure.lang.APersistentMap java.io.Writer))
(defmacro defrecord [name fields & protocols]
  `(clj.third-party.defrecord2/defrecord2 ~name ~fields ~@protocols))
 
;;taken and modified from http://clojure101.blogspot.com/2009/04/destructuring-binding-support-in-def.html

(defmacro def+
  "def with binding (def+ {:keys [a b d]} {:a 1 :b 2 :d 3})"
  [a b]
  (let [let-expr (macroexpand `(let [~a ~b]))
        vars
	(filter #(not (.contains (str %) "__"))
		(map first (partition 2 (second let-expr))))
        def-vars (map (fn [v] `(def ~v ~v)) vars)]
    (concat let-expr def-vars)))

;;TODO: serializable records and atoms and refs 
(defmulti swap! (fn [a & args] (class a)))
(defmethod swap! clojure.lang.Atom [& args]
  (apply clojure.core/swap! args))
(defmulti reset! (fn [a & args] (class a)))
(defmethod reset! clojure.lang.Atom [& args]
  (apply clojure.core/reset! args))
(defprotocol PWait 
  (wait-for [this timeout units] [this timeout]))
;;copied from clojure source, but adding timeout wait-for
(defn promise
  "Alpha - subject to change.
  Returns a promise object that can be read with deref/@, and set,
  once only, with deliver. Calls to deref/@ prior to delivery will
  block. All subsequent derefs will return the same delivered value
  without blocking."
  {:added "1.1"}
  []
  (let [d (java.util.concurrent.CountDownLatch. 1)
        v (atom nil)]
    (reify 
      clojure.lang.IDeref
      (deref [_] (.await d) @v)
      PWait
      (wait-for [this timeout]
                (wait-for this timeout
                          java.util.concurrent.TimeUnit/MILLISECONDS))
      (wait-for [this timeout units]
                (if timeout
                  (.await d timeout units)
                  (do (.await d) true)))
      clojure.lang.IFn
      (invoke [this x] 
              (locking d
                (if (pos? (.getCount d))
                  (do (reset! v x)
                      (.countDown d)
                      x)
                  (throw
                   (IllegalStateException.
                    "Multiple deliver calls to a promise"))))))))

(defmethod print-dup clojure.lang.Atom [o w]
  (.write w "#=(clojure.core/atom ") (print-dup @o w) (.write w ")"))
(defmethod print-dup clojure.lang.Ref [o w]
  (.write w "#=(clojure.core/ref ") (print-dup @o w) (.write w ")"))

