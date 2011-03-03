
(ns forex.util.emacs (:use forex.util.general))
 
(defn fn-meta [function meta]
  (let [ns-from *ns*]
    (proxy [clojure.lang.AFn clojure.lang.Fn  clojure.lang.IMeta] []
      (invoke [& args] (apply function args))
      (meta [] (merge (meta function) (merge {:ns ns-from} meta))))))

(defn- val-of [a] (if (var? a) (var-get a) a)) 
;;TODO: add log to this
(defn- apply-fn [a args]
  (try (apply (val-of a) args)
       (catch Exception e
         (println (format "error in hook %s %s: %s" a (val-of a) e)))))
(defmacro- run-fn [a & args]
  `(let [a# ~a]
     (try ((val-of a#) ~@args)
          (catch Exception e# (println 
                               (format "error in hook %s %s: %s" a# (val-of a#) e#))))))

(defn- as-ns [a]
  (condp = (class a)
      String (find-ns (symbol a))
      clojure.lang.Symbol (find-ns a)
      a))
(defn ns-metas
  ([fn] (ns-metas *ns* fn))
  ([ns fn]
     (is (as-ns ns) "%s is not a ns, or cant find it" ns)
     (let [vars (filter fn (vals (ns-interns (as-ns ns))))]
       (apply hash-map (interleave vars (map var-get vars))))))
(defn ns-vars
  ([] (ns-vars *ns*))
  ([ns]
     (is (as-ns ns) "%s is not an ns, or cant find it" ns)
     (let [vars (filter #(:var (meta  %)) (vals (ns-interns (as-ns ns))))]
       (apply hash-map (interleave vars (map var-get vars))))))

(defmacro defvar
  ([name]
     (let [new-name (with-meta name (assoc (meta name) :var true))]
       `(defonce ~new-name nil)))
  ([name init]
     (let [new-name (with-meta name (assoc (meta name) :var true))]
       `(defonce ~new-name ~init))))
(defmacro defhook [& args] `(defvar ~@args)) 
(defmacro setq [& args]
  `(do ~@(map
          (fn [[var val]]
            `(alter-var-root #'~var (fn [a#] ~val)))
          (group args))))


(defn- member
  ([value list] (member value list =))
  ([value list test]
     (some #(test % value) list)))

(defn- pushnew* [hook arg id replace] 
  (if replace 
    (alter-var-root hook (fn [old]
			   (doall (concat
				   (list arg)
				   (filter #(and (if id (not (= (:id (meta %)) id)) true)
						(not (= % arg))) old)))))
    (alter-var-root hook (fn [old] 
			   (doall (if (empty? (take 1 (filter #(or (when id (= (:id (meta %)) id)) (= % arg)) old)))
				    (concat (list arg) old)
				    old))))))
(defmulti pushnew (fn [a b & args] [(if (fn? a) ::fn (type a)) (if (fn? b) ::fn (type b))]))
(defmethod pushnew [clojure.lang.Var ::fn]
  ([hook function] (pushnew hook function *ns* true))
  ([hook function id] (pushnew hook function id true))
  ([hook function id replace] 
     (let [new-function (if (:id (meta function)) function (if id (fn-meta function {:id id}) function))]
       (pushnew* hook new-function id replace))))
(defmethod pushnew [clojure.lang.Var clojure.lang.Var]
  ([hook var] (pushnew hook var nil false))
  ([hook var id] (pushnew hook var id false)) 
  ([hook var id replace] (pushnew* hook var id replace)))


(defn- add-to-list* [hook arg id replace] 
  (if replace 
    (alter-var-root hook (fn [old]
			   (doall (concat
				   (filter #(and (if id (not (= (:id (meta %)) id)) true)
						(not (= % arg))) old)
				   (list arg)))))
    (alter-var-root hook (fn [old]
			   (doall (if (empty? (take 1 (filter #(or (when id (= (:id (meta %)) id)) (= % arg)) old)))
				    (concat old (list arg))
				    old))))))
(defmulti add-to-list (fn [a b & args] [(if (fn? a) ::fn (type a)) (if (fn? b) ::fn (type b))]))
(defmethod add-to-list [clojure.lang.Var ::fn]
  ([hook function] (add-to-list hook function *ns* true))
  ([hook function id] (add-to-list hook function id true))
  ([hook function id replace] 
     (let [new-function (if (:id (meta function)) function (if id (fn-meta function {:id id}) function))]
       (add-to-list* hook new-function id replace))))
(defmethod add-to-list [clojure.lang.Var clojure.lang.Var]
  ([hook var] (add-to-list hook var nil false))
  ([hook var id] (add-to-list hook var id false)) 
  ([hook var id replace] (add-to-list* hook var id replace)))

(comment
  (defn push [var val]
    (alter-var-root var (fn [it] (concat (list val) it))))) 

(defn add-hook [hook function] (pushnew hook function))
(defn add-hooks [hook functions] (doall (map #(pushnew hook %) functions)))

;;RUNNING hooks
(defn run-hooks [& hooks]
  (mapc (fn [hook] (mapc #(run-fn %) hook)) hooks))

(defn run-hook-with-args [hook & args]
  (mapc #(apply-fn % args) hook))


(defn run-hook-with-args-until-success [hook & args]
  (is (sequence? hook) "hook %s isnt a list" hook)
  (loop [funcs hook]
    (cond
      (empty? funcs) nil
      true (if-let [it (apply-fn (first funcs) args)]
             it
             (recur (rest funcs)))))) 

(defn run-hook-with-args-until-failure [hook & args]
  (is (sequence? hook) "hook %s isnt a list" hook)
  (loop [funcs hook]
    (cond
      (empty? funcs) true
      true (when (apply-fn (first funcs) args)
             (recur (rest funcs))))))


;;(run-hook-with-args-until-success 'a 3)

