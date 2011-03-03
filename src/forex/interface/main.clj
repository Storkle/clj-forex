
(ns forex.interface.main
  (:require swank.swank)
  (:use forex.interface.gui forex.interface.ea-table forex.interface.ea-new)
  (:use forex.util.emacs forex.util.gui forex.interface.tray))
 
(defn on-system-exit [icon]
  (when (prompt (format "Do you really want to exit %s?
This will stop all running expert advisors." gui-frame-title))
    (System/exit 0)))
  
(defn -main [& args]
  (let [user-exception 
        (try
          (require 'forex-user) 
          nil
          (catch Exception e 
            (.printStackTrace e)
            e))] 
    (add-to-list  
     #'gui-on-exit-hooks on-system-exit)
    (invoke-new-gui (when user-exception "error when loading forex-user: check log"))))


