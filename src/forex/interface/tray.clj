
(ns forex.interface.tray
  (:use forex.util.gui)
  (:import                        
   (javax.swing JFrame)
   (java.awt Toolkit SystemTray  EventQueue TrayIcon TrayIcon$MessageType PopupMenu MenuItem)
   (java.awt.event ActionListener)))

(defn remove-system-icon [icon] (.remove (SystemTray/getSystemTray) icon))
(defn- setup-popup-menu
  ([menu icon] (setup-popup-menu menu icon (constantly true)))
  ([menu icon on-exit]
     (let [exit (MenuItem. "Exit")]
       (add-action-listener
        exit (if on-exit
               (bound-fn [e] (try
                               (on-exit icon)
                               (catch Exception e
                                 (.printStackTrace e)
                                 (println "ERROR IN ON-EXIT %s" e))))
               (fn [e] (remove-system-icon icon))))
       (.add menu exit)
       menu)))

(defn system-icon [{:keys [image on-exit on-click on-exit]}]
  {:pre [(string? image)]}
  (let [menu (PopupMenu.)
        tray-icon
        (doto (TrayIcon. (.getImage (Toolkit/getDefaultToolkit) image)
                         nil menu)
          (.setImageAutoSize true))]
    (setup-popup-menu menu tray-icon on-exit) 
    (when on-click (add-action-listener tray-icon (bound-fn [e] (try (on-click tray-icon) (catch Exception e (println "ERROR IN ON-CLICK %s" e))))))  
    tray-icon)) 
(defn add-system-icon [icon] (.add (SystemTray/getSystemTray) icon) icon)

(defn icon-info
  ([icon txt] (icon-info icon "info" txt))
  ([icon caption txt] (.displayMessage icon caption txt TrayIcon$MessageType/INFO)))
(defn icon-warning
  ([icon txt] (icon-warning icon "warning" txt))
  ([icon caption txt] (.displayMessage icon caption txt TrayIcon$MessageType/WARNING)))
(defn icon-error
  ([icon txt] (icon-error icon "error" txt))
  ([icon caption txt] (.displayMessage icon caption txt TrayIcon$MessageType/ERROR)))

;;(def icon (add-system-icon (system-icon {:image  "/home/seth/Desktop/clojure.gif" :on-click (fn [_] (println "HI"))})))


