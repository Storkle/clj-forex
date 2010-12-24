(ns forex.module.gui.common 
  (:use
   forex.module.gui.core
   forex.util.general
   emacs utils.general utils.fiber.spawn
   forex.backend.mql.socket_service))

(defn new-chart [symbol]
  (receive (format "ChartWindow %s" symbol)))

(defn hline
  ([price] (hline price :white))
  ([price color] (hline price color 0))
  ([price color style] 
     (let [name (str "hline_" (make-tag))]
       (object-create name +obj_hline+ 0 price)
       (set-color name color)
       (set-style name style)
       {:name name :color color :style style})))

 