
(clojure.core/use 'nstools.ns)
(ns+ forex.examples.renko.renko
     (:clone forex.default) 
     (:use forex.util.general)) 
(register-ea)
;;TODO: i have some cases in which backend sockets get confused and sends something to a dead socket!
;; sigh - i really dont want to do that other socket architecture

;;TODO: *ea-stop* already refers to: #'forex.module.ea/*ea-stop* in namespace: forex.default 
;;TODO: on GBP/JPY, why did it enter so late???? 
;;TODO!!!: speed up ns+ by alot! and dont have it automatically reload it, please!!!

;;TODO: test reusing of account.utils
;;TODO: defaults per currency
(def ^{:var true} state (atom :monitor)) 
(def ^{:var true} order (atom-hash))
(def ^{:var true} trail-time-period 5)

(def main (partial vma [2 2 100 1]))
(def signal (partial vma [2 2 1 1] ))
(def middle (partial vma [2 2 26 1]))
(def support (blazan-dynamic-stop [150 70 1 10000] 1))
(def resistance (blazan-dynamic-stop [150 70 1 10000] 0))

;;UTILS
(defn up? [signal i]
  (let [diff (- (signal i) (signal (+ i 1)))]
    (if (>= diff 0) 
      diff
      false))) 
(defn down? [signal i]
  (let [diff (- (signal i) (signal (+ i 1)))]
    (if (<= diff 0)
      diff
      false)))    
(defn reset []
  (reset! state :monitor) (reset! order nil) (out "order closed"))
(defn calculate-lots [] (lot (* 0.01 (/ (account-balance) 100))))
;;TODO: use symbol in o for profit! 
(defn profit
  ([o] (wenv {:symbol (or (:symbol o) (env :symbol))} (profit o (close) (:break o))))
  ([o a] (profit o a (:break o)))
  ([o a b] (point (o-- o a b))))

;;TODO: exit half when cross middle? enter half more when recross? 
;;OPEN CONDITION 
(defn open-condition [i]  
  (if-let [[dir i] (cross? signal main i)]
    (or (and (= dir :buy) (> (signal i) (middle i)) {:type :buy :i i :category  :long :state :init})
        (and (= dir :sell) (< (signal i) (middle  i)) {:type :sell :i i :category :long :state :init}))
    (when-let [[dir i] (cross? signal middle i)]
      (or (and (= dir :buy) ;;TODO: better reentry , oui? slope? 
               (or (and (< (middle i) (main i)) (> (middle (+ i 10)) (main (+ i 10)))
                        {:type :buy :i i :category :long :state :init}) ;;the dip
                   (and (<= (Math/abs (- (main i) (middle i))) (pip 20)) (or (up? main i) (up? main (+ i 1)))
                        (> (middle i) (main i)) 
                        ;;{:type :buy :i i :category :scalp}
                        nil
                        ))) ;;thin 
          (and (= dir :sell)
               (or (and (> (middle i) (main i)) (< (middle (+ i 10)) (main (+ i 10)))
                        {:type :sell :i i :category :long :state :init}) ;;the dip
                   (and (<= (Math/abs (- (main i) (middle i))) (pip 20)) (or (down? main i) (down? main (+ i 1)))
                        (< (middle i) (main i))
                        ;;{:type :sell :i i :category :scalp}
                        nil
                        )))))))   

;;ORDER MANAGEMENT
(defmulti close-condition (fn [o & args] (:category o)))
(defmulti check (fn [o & args] (:category o)))
;;;SCALP ORDER MANAGEMENT
(defn should-close? [{:keys [type] :as o}] 
  (cond
   (= type :buy)
   (or (< (signal 1) (main 1)) (< (signal 1) (middle 1)))
   (= type :sell)
   (or (> (signal 1) (main 1)) (> (signal 1) (middle 1)))))

;;;LONG ORDER MANAGEMENT
;;TODO: take profit via price action !!!!; if GBPJPY is owning, why not trail on 1 minute level? basically, alot of this take profit stuff and entering
;;and exiting is based on a variety of conditions! Im sensing some AI like programming taking place! doesn't need to be ai, but how about using
;;programming which takes into account the context? see http://web.media.mit.edu/~push/ and the phd thesis EM-ONE: An Architecture for Reflective Commonsense Thinking

(defn cci-close?
  "This is a tight virtual stop on the five minute level. 
   For buy, if cci>100, set cross-sl to previous middle value.
   If signal(1) is less than cross-sl, then exit. Opposite for sell"
  [{:keys [type state] :as o} period] 
  (when (= state :trail) 
    (wenv {:period period}    
          ;;modify cross-sl 
          (when (or (> (cci 30 1) 100) (< (cci 30 1) -100))
            ;;we will not update the cross-sl so that it will generate  a close signal, this would be bad!
            (when (or (and (buy? o) (> (signal 1) (middle 1)))
                      (and (sell? o) (< (signal 1) (middle 1))))
              (modify! o {:cross-sl (middle 1)})))  
          ;;check close condition
          (when-let [cross-sl (:cross-sl o)]
            (or (and (buy? o) (<= (signal 1) cross-sl)) 
                (and (sell? o) (>= (signal 1) cross-sl))))))) 

(defmethod close-condition :long [{:keys [type state] :as o}] 
  (if (= state :init)
    (and (should-close? o)
         (do (out "init close") true)) ;;TODO: GBPUSD screwed up for -30 pips how to effectively close this? how bout pick better entry poitns? or dont pick exhaustion
    (or (and (cond
              (= type :buy)
              (< (signal 1) (main 1))
              (= type :sell)
              (> (signal 1) (main 1)))
             (do (out "cross close") true))
        (and (cci-close? o trail-time-period) (do (out "cci close") true))))) 

(defmethod check :long [o]
  (when (and (>= (profit o) 20) (not (= (:state o) :trail))) (modify! o {:state :trail}) (out "changed to trail"))
  ;;actual sl the server sees. this is a safety stop, in case our ea gets screwed up, turns off, etc.
  (when (or (= (:sl o) 0) (= (:state o) :trail))
    (modify! o {:sl (omax o (:sl o) (o- o (main 1) (pip 20)))})))  
;;(run)
;;MAIN  
(defn gobble [o] 
  (if (close? o) 
    (reset)
    (awhen-not (and (close-condition o) (close! o))
               (check o))))

(defn monitor []  
  (when-let [{:keys [type category] :as o} (open-condition 0)] 
    (awhen (order! (merge o  {:type type :lots (calculate-lots)})) 
           (reset! order it)
           (reset! state :gobble)   
           (out "entered %s %s order" type category))))

(defn start [args] 
  (cond
   (= @state :monitor) (monitor)
   (= @state :gobble) (gobble order)))

;;(defn vars [ea] (:vars ea))
;;(defn getv [vvar ea] (get (:vars ea) vvar))


