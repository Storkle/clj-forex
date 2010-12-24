(ns forex.module.gui.core
  (:use
   forex.util.general
   emacs utils.general utils.fiber.spawn
   forex.backend.mql.socket_service))

;;;MANIPULATING OBJECTS
(constants 
 ;;timeframes
 +m1+ 33137 
 +m5+ 33138
 +m15+ 33139
 +m30+ 33140
 +h1+ 35400
 +h4+ 33136
 +d1+ 33134 
 +w1+ 33141 
 +mn+ 33334
 ;;object types
 +obj_vline+ 0
 +obj_hline+ 1
 ;;object properties
 +p_color+ 6
 +p_style+ 7
 +p_time1+ 0
 +p_price1+ 1
 +p_time2+ 2
 +p_price2+ 3
 +p_time3+ 4
 +p_price3+ 5)

(defn object-set [name index value]
  (receive! (format "ObjectSet %s %s %s" name index value)))

(defn set-style [name val]
  (is (and (integer? val) (<= val 4) (>= val 0)))
  (object-set name +p_style+ val))

(defn set-color [name val]
  (object-set name +p_color+
	      (cond
		(= val :red) 230
		(= val :yellow) 65535
		(= val :green) 65280
		(= val :blue) 13749760
		(= val :purple) 16711935
		(= val :white) 16777215
		(= val :black) 0
		true (throwf "invalid color %s" val))))
  
(defn delete-all []
  (receive! "DeleteAll"))
(defn object-delete [name]
  (receive! (str "ObjectDelete " name)))
(defn object-get [name type] 
  (receive! (format "ObjectGet %s %s" name type)))
(defn object-create
  ([name type time1 price1]
     (object-create name type time1 price1 0 0 0 0))
  ([name type time1 price1 time2 price2]
     (object-create name type time1 price1 time2 price2 0 0))
  ([name type time1 price1 time2 price2 time3 price3]
     (receive! (format "ObjectCreate %s %s %s %s %s %s %s %s"
		       name type time1 price1 time2 price2 time3 price3))))

 
;;;;MANIPULATIONS THROUGH WINDOW MESSAGING
(defn set-focus [symbol timeframe]
  (receive! (format "SetFocus %s %s" symbol +m1+)))
(defn change-timeframe [symbol timeframe]
  (receive! (format "ChangeTimeframe %s %s" timeframe symbol)))
(defn switch-to-symbol [symbol timeframe]
  (change-timeframe symbol timeframe) 
  (set-focus symbol timeframe)) 
(defn post [symbol num]
  (receive! (format "Post %s %s" symbol num)))
(defn toggle-grid [sym] (post sym 33021))
(defn shift [sym] (post sym 33023))
(defn zoom-out
  ([sym] (zoom-out sym true))
  ([sym sh] (post sym 33026) (if sh (shift sym))))
(defn zoom-in
  ([sym] (zoom-in sym true))
  ([sym sh ] (post sym 33025) (if sh (shift sym))))
(defn zoom-out-all [sym]
  (dorun (repeatedly 5 (partial zoom-out sym false)))
  (shift sym))
(defn zoom-in-all [sym]
  (dorun (repeatedly 5 (partial zoom-in sym false)))
  (shift sym))   
(defn bars [sym] (post sym 33018))
(defn candles [sym] (post sym 33019))
(defn lines [sym] (post sym 33022))
(defn save-picture [sym] (post sym 33054))
(defn horizontal [sym] (post sym 33244))
(defn trendline [sym] (post sym 33257))

