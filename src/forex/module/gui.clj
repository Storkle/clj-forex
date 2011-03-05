(clojure.core/use 'nstools.ns)
(ns+ forex.module.gui
     (:clone clj.core)
     (:use forex.module.indicator.util)
     (:use forex.util.emacs
	   forex.module.indicator
	   [clj-time.core :exclude [extend start]]
	   [clj-time.coerce]
	   forex.util.spawn
	   forex.util.core
	   forex.util.log
	   forex.util.general
	   forex.module.account.utils 
	   forex.module.error)   
     (:require clojure.contrib.core
      [forex.backend.mql.socket-service :as backend])
     (:require [forex.module.error :as s])) 


(def gui-property-map
  {:time1 0
   :price1 1
   :time2 2
   :price2 3
   :time3 4
   :price3 5
   :color 6
   :style 7
   :width 8
   :back 9
   :ray 10
   :ellipse 11
   :scale 12
   :angle 13
   :arrowcode 14
   :timeframes 15
   :deviation 16
   :fontsize 100
   :corner 101
   :xdistance 102
   :ydistance 103
   :fibolevels 200
   :levelcolor 201
   :levelstyle 202
   :levelwidth 203})

(def *gui-redraw* true)
;;(def *gui-objects* [])

(def *redraw-nest* 0)
(defn window-redraw []
  (request "WindowRedraw"))

(defmacro- redraw [& body]
  `(binding [*redraw-nest* (inc *redraw-nest*)]
     (let [result# (do ~@body)]
       (when (and *gui-redraw* (= *redraw-nest* 1))
	 (window-redraw))
       result#)))
 
(defn objects-total []
  (receive! "ObjectsTotal"))
(defn objects-delete-all []
  (redraw (receive! "ObjectsDeleteAll")))
(defn object-delete [name]
  (redraw (receive! (format "ObjectDelete ;%s" name))))
(defn object-name
  ([] (object-name 0))
  ([i] (receive! (format "ObjectName %d" i))))
(defn object-description
  ([] (object-description 0))
  ([i] (receive! (format "ObjectDescription ;%s" i))))

(defn object-get [name i]
  (if-let [it (or (get gui-property-map i) (and (number? i) (>= i 210) (<= i (+ 210 31)) i))]
    (receive! (format "ObjectGet %d ;%s" it name))
    (throwf "invalid object property type %s" i)))

(defn object-names []
  (for [i (range 0 (objects-total))]
    (object-name i)))

(defn object-set [name i val]
  (redraw
   (if-let [it (or (get gui-property-map i) (and (>= i 210) (<= i (+ 210 31)) i))]
     (receive! (format "ObjectSet %d %s;%s" it val name))
     (throwf "invalid object property type %s" i)))) 


(def gui-type-map
  {:vline 0
   :hline 1
   :trend 2
   :trendbyangle 3
   :regression 4
   :channel 5
   :stddevchannel 6
   :gannline 7
   :gannfan 8
   :ganngrid 9
   :fibo 10
   :fibotimes 11
   :fibofan 12
   :fiboarc 13
   :expansion 14
   :fibochannel 15
   :rectangle 16
   :triangle 17
   :ellipse 18
   :pitchfork 19
   :cycles 20
   :text 21
   :arrow 22
   :label 23})
(defn object-create
  [{:keys [name type time1 price1 time2 price2 time3 price3]
    :or {name (str (gensym)) time2 0 price2 0 time3 0 price3 0
	 price1 0 time1 0}}]
  (redraw
   (let [type (get gui-type-map type)]
     (is? type "invalid gui type %s" type)
     (when (receive! (format "ObjectCreate %s %d %s %d %s %d %s ;%s" type time1 price1 time2 price2 time3 price3 name))
       name))))  

(def gui-color-map
  {:blue 1.671168E7})
(def gui-style-map
  {:solid 0 :dash 1 :dot 2 :dashdot 3 :dashdotdot 4})

(defn get! [map val] (let [ret (get map val)] (is? ret "couldnt get key %s in map %s" val map) ret))

(defn hline [price & {:keys [color style name] :or {style :solid name (str (gensym)) color :blue}}]
  {:pre [(number? price)]}
  (doto (object-create {:type :hline :name name :price1 price})
    (object-set :color (or (get gui-color-map color) color))
    (object-set :style (get! gui-style-map style))))
 
(defn vline [time & {:keys [color style name] :or {style :solid name (str (gensym)) color :blue}}]
  {:pre [(number? time)]}
  (doto (object-create {:type :vline :name name :time1 time})
    (object-set :color (or (get gui-color-map color) color))
    (object-set :style (get! gui-style-map style))))


;;(object-create {:name "blazan dynamic stop" :type :hline :price1 1.386})