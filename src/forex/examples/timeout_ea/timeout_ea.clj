
;;forex.examples.timeout-ea : ea which manages the orders of a trade
(clojure.core/use 'nstools.ns)
(ns+ forex.examples.timeout-ea.timeout-ea
     (:clone forex.gui) 
     (:use forex.examples.timeout-ea.utils
           forex.interface.gui)
     (:import (javax.swing JScrollPane JTextField)))
(register-ea)
 ;;ea vars   
(defvar state (atom :timeout))
(defvar end-time)
(defvar order) 
;; 
(declare timeout break-even trail)

;;TODO: when we order, and modify fails, how do we get the error? later :)...
(defn init [{:keys [type sl price hour lots tp2]}]
  (aif (order! {:type type :sl sl :price price :lots lots :tp tp2
                })
       {#'end-time (plus (now) (hours hour))
        #'order (atom-hash it)} 
       (:e it)))  

(defn start [args]
  (cond
   (close? order) (exit "order is now closed")
   (= @state :timeout) (timeout args)
   (= @state :break-even) (break-even args)
   (= @state :trail) (trail))) 

(defn timeout [_]
  (cond
   ;;changed to market
   (market? order)
   (do (reset! state :break-even) (out "order opened - monitoring tp1"))
   ;;entry order reach sl 
   (if (sell? order) (> (close) (:sl order)) (< (close) (:sl order)))
   ;;TODO: reliable delete????
   (awhen (delete! order) (exit "entry order reached sl. deleting ..."))
   ;;timed out
   (after? (now) end-time)
   (awhen (delete! order) (exit "order timed out"))))
 
;;TODO: better sl saving 
(defn break-even [{:keys [sl tp2 tp1]}]
  (if (hit? order tp1)
    (do (out "closing to half ...") 
	(awhen (-> (modify! order {:sl (:break order) :tp 0}) 
		   (close! (lot (/ (:lots order) 2))))	       
	       (reset! state :trail) 
	       (out "trailing order")))
    (modify! order {:sl sl :tp tp2})))   
 
(defn hh [a] 
  (apply max (map high (range 1 (inc a)))))
(defn ll [a]
  (apply min (map low (range 1 (inc a)))))
(defn trail []
  (wenv {:period 2}
	(when (or (and (sell? order) (< (cci 14 1) -100))
		  (and (buy? order) (> (cci 14 1) 100)))
	  (modify! order
		   {:sl (omax order
			      (:sl order)
			      (if (sell? order)
				(+ (hh 3) (pip 2))
				(- (ll 3) (pip 2))))})))) 
;;;GUI   
(defn init-gui [] 
  (let [panel (JPanel.)
	text (JTextArea. 20 30)
	risk (JTextField. "1" 7)
        frame (jframe "timeout ea")]
    (doto frame
      (.add (miglayout
             panel :layout "center" :column "center" :row "center"
             (JLabel. "paste email text") :wrap
             (JScrollPane. text) "span,grow" :wrap
	     risk (JLabel. "% risk") :wrap
             (doto (JButton. "ok")
               (add-action-listener
                (fn [e]
		  (try
		    (let [risk (Double/parseDouble (.getText risk))
			  parsed (try (match-method risk (.getText text)) (catch Exception e nil))]
		      (if (= 0 (:lots parsed))
			(inform "not enough $ to trade order %s" parsed)
			(if parsed
			  (when (prompt "is order %s ok?" parsed)
			    (with-out-str+ out
			      (if-not (run parsed) 
				(inform "failed to run ea %s" (str out))
				(.setVisible frame false)))) 
			  (inform "failed to match input text"))))
		    (catch Exception e (inform "invalid risk %s" (.getText risk)))))))
             "w 50%"
             (doto (JButton. "cancel")
               (add-action-listener (fn [_] (.setVisible frame false))))
             "w 50%"))
      (.pack)
      (.setVisible true))))

