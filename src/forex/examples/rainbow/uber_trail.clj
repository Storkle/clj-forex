(clojure.core/use 'nstools.ns)
(ns+ forex.examples.rainbow.uber-trail
     (:clone forex.default) 
     (:use forex.util.general forex.examples.rainbow.uber-trail))

(defn profit
  ([o] (wenv {:symbol (or (:symbol o) (env :symbol))} (profit o (close) (:break o))))
  ([o a] (profit o a (:break o)))
  ([o a b] (point (o-- o a b))))


;;TODO: setup a trail object?
(def- main (partial vma [2 2 100 1]))
(def- signal (partial vma [2 2 1 1] )) 
(def- middle (partial vma [2 2 26 1]))

(def- support (partial blazan-dynamic-stop [150 70 1 10000] 1))
(def- resistance (partial blazan-dynamic-stop [150 70 1 10000] 0))
(defonce- trail-time-period +m5+)

(defn uber-init-close? [{:keys [type] :as o}] 
  (cond
   (= type :buy)
   (or (< (signal 1) (main 1)) (< (signal 1) (middle 1)))
   (= type :sell)
   (or (> (signal 1) (main 1)) (> (signal 1) (middle 1)))))
 
(defn uber-trail-close?
  "This is a tight virtual stop on the five minute level. 
   For buy, if cci>100, set cross-sl to previous middle value.
   If signal(1) is less than cross-sl, then exit. Opposite for sell"
  [{:keys [type uber-trail-state] :as o} period] 
  (when (= uber-trail-state :trail) 
    (wenv {:period period}    
          ;;modify cross-sl 
          (when (or (> (cci 30 1) 100) (< (cci 30 1) -100))
            ;;we will not update the cross-sl so that it will generate  a close signal, this would be bad!
            (when (or (and (buy? o) (> (signal 1) (middle 1)))
                      (and (sell? o) (< (signal 1) (middle 1))))
              (modify! o {:uber-trail-cross-sl (middle 1)})))  
          ;;check close condition
          (when-let [cross-sl (:uber-trail-cross-sl o)]
            (or (and (buy? o) (<= (signal 1) cross-sl)) 
                (and (sell? o) (>= (signal 1) cross-sl))))))) 


(defn uber-close? [{:keys [type uber-trail-state] :as o}] 
  (if-not (= uber-trail-state :trail)
    (and (uber-init-close? o)
         (do (out "uber init close") true)) 
    (or (and (cond
              (= type :buy)
              (< (signal 1) (main 1))
              (= type :sell)
              (> (signal 1) (main 1)))
             (do (out "uber trail close - ma cross") true))
        (and
	 (uber-trail-close?  o trail-time-period) 
	 (do (out "uber trail close - hit virtual sl") true)))))

(defn uber-trail-update [o]
  (when (and (>= (profit o) 20)
	     (not (= (:uber-trail-state o) :trail)))
    (modify! o {:uber-trail-state :trail})
    (out "changed to trail"))
  ;;actual sl the server sees. this is a safety stop, in case our ea gets screwed up, turns off, etc. 
  (when (or (= (:sl o) 0) (= (:uber-trail-state o) :trail))
    (modify! o {:sl (omax o (:sl o) (o- o (main 1) (pip 20)))})))

(defn uber-trail! [o]
  (if (close? o)
    true
    (do
     (uber-trail-update o) 
     (awhen (and (uber-close? o) (close! o)) true))))


